import asyncio
import logging
import os
import re
import shutil
import time
import uuid
from pathlib import Path

import yt_dlp
from aiogram import Bot, Dispatcher, F, Router
from aiogram.client.default import DefaultBotProperties
from aiogram.enums import ParseMode
from aiogram.filters import CommandStart
from aiogram.types import CallbackQuery, FSInputFile, InlineKeyboardButton, InlineKeyboardMarkup, Message

BOT_TOKEN = os.getenv("BOT_TOKEN", "").strip()
MAX_FILE_MB = int(os.getenv("MAX_FILE_MB", "48"))
MAX_FILE_BYTES = MAX_FILE_MB * 1024 * 1024
JOB_TTL = 60 * 60

BASE = Path("/tmp/najm_downloads")
BASE.mkdir(parents=True, exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s",
)
log = logging.getLogger("najm-downloader")

router = Router()
jobs = {}
download_lock = asyncio.Lock()
URL_RE = re.compile(r"https?://[^\s<>()]+", re.I)


def clean_url(text):
    m = URL_RE.search(text or "")
    return m.group(0).rstrip(".,،؛;)") if m else None


def ydl_opts():
    return {
        "quiet": True,
        "no_warnings": True,
        "noplaylist": True,
        "socket_timeout": 30,
        "retries": 5,
        "fragment_retries": 5,
        "file_access_retries": 3,
        "concurrent_fragment_downloads": 2,
    }


def probe(url):
    opts = ydl_opts()
    opts["skip_download"] = True
    with yt_dlp.YoutubeDL(opts) as ydl:
        info = ydl.extract_info(url, download=False)

    if info and info.get("_type") in {"playlist", "multi_video"}:
        entries = [x for x in (info.get("entries") or []) if x]
        if not entries:
            raise RuntimeError("لم يتم العثور على فيديو داخل الرابط.")
        info = entries[0]

    return {
        "title": info.get("title") or "فيديو",
        "url": info.get("webpage_url") or url,
        "extractor": info.get("extractor_key") or info.get("extractor") or "Unknown",
        "duration": info.get("duration"),
    }


def human_duration(seconds):
    if not seconds:
        return "غير معروف"
    try:
        seconds = int(seconds)
    except Exception:
        return "غير معروف"
    m, s = divmod(seconds, 60)
    h, m = divmod(m, 60)
    return f"{h:02d}:{m:02d}:{s:02d}" if h else f"{m:02d}:{s:02d}"


def result_file(folder):
    files = [
        p for p in folder.iterdir()
        if p.is_file() and not p.name.endswith((".part", ".ytdl", ".temp", ".json"))
    ]
    if not files:
        raise RuntimeError("لم يتم العثور على الملف الناتج.")
    return max(files, key=lambda p: p.stat().st_mtime)


def download_media(url, mode, quality, folder):
    folder.mkdir(parents=True, exist_ok=True)
    opts = ydl_opts()
    opts["outtmpl"] = str(folder / "najm_%(id).40B.%(ext)s")

    if mode == "audio":
        opts.update({
            "format": "bestaudio/best",
            "postprocessors": [{
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "192",
            }],
        })
    else:
        h = int(quality or 0)
        if h == 0:
            fmt = (
                "best[ext=mp4]/"
                "bestvideo[ext=mp4]+bestaudio[ext=m4a]/"
                "bestvideo+bestaudio/best"
            )
        else:
            fmt = (
                f"best[height<={h}][ext=mp4]/"
                f"bestvideo[height<={h}][ext=mp4]+bestaudio[ext=m4a]/"
                f"best[height<={h}]/best"
            )
        opts.update({
            "format": fmt,
            "merge_output_format": "mp4",
        })

    with yt_dlp.YoutubeDL(opts) as ydl:
        ydl.extract_info(url, download=True)

    return result_file(folder)


def keyboard(jid):
    return InlineKeyboardMarkup(inline_keyboard=[
        [InlineKeyboardButton(text="⭐ أفضل جودة", callback_data=f"dl:{jid}:v:0")],
        [
            InlineKeyboardButton(text="📹 1080p", callback_data=f"dl:{jid}:v:1080"),
            InlineKeyboardButton(text="📹 720p", callback_data=f"dl:{jid}:v:720"),
        ],
        [
            InlineKeyboardButton(text="📹 480p", callback_data=f"dl:{jid}:v:480"),
            InlineKeyboardButton(text="📹 360p", callback_data=f"dl:{jid}:v:360"),
        ],
        [InlineKeyboardButton(text="🎵 MP3", callback_data=f"dl:{jid}:a:0")],
        [InlineKeyboardButton(text="❌ إلغاء", callback_data=f"cancel:{jid}")],
    ])


def purge_old_jobs():
    now = time.time()
    stale = [jid for jid, item in jobs.items() if now - item.get("created", now) > JOB_TTL]
    for jid in stale:
        jobs.pop(jid, None)


@router.message(CommandStart())
async def start(message: Message):
    await message.answer(
        "⭐ <b>نجم تحميل</b>\n\n"
        "أرسل رابط فيديو مباشرة، ثم اختر الجودة أو MP3.\n"
        "النسخة مستقرة لمستخدم واحد وتمنع تداخل عمليتي تحميل.\n\n"
        "استخدمها فقط للمحتوى الذي تملك حق تنزيله أو لديك إذن بتنزيله."
    )


@router.message(F.text)
async def handle_url(message: Message):
    purge_old_jobs()
    url = clean_url(message.text)
    if not url:
        await message.answer("🔗 أرسل رابط فيديو صحيح.")
        return

    status = await message.answer("🔎 جاري فحص الرابط...")
    try:
        info = await asyncio.wait_for(asyncio.to_thread(probe, url), timeout=60)
        jid = uuid.uuid4().hex[:10]
        jobs[jid] = {
            "url": info["url"],
            "user_id": message.from_user.id if message.from_user else 0,
            "created": time.time(),
        }
        await status.edit_text(
            f"✅ <b>{info['title']}</b>\n"
            f"🌐 المنصة: <code>{info['extractor']}</code>\n"
            f"⏱ المدة: {human_duration(info['duration'])}\n\n"
            "اختر صيغة التحميل:",
            reply_markup=keyboard(jid),
        )
    except asyncio.TimeoutError:
        await status.edit_text("⏱ انتهت مهلة فحص الرابط. أرسله مرة أخرى.")
    except Exception as e:
        log.exception("probe error")
        await status.edit_text(
            "❌ تعذر قراءة الرابط.\n"
            f"<code>{str(e)[:220]}</code>"
        )


@router.callback_query(F.data.startswith("cancel:"))
async def cancel_job(call: CallbackQuery):
    jobs.pop(call.data.split(":", 1)[1], None)
    await call.answer("تم الإلغاء")
    if call.message:
        await call.message.edit_text("❌ تم إلغاء العملية.")


@router.callback_query(F.data.startswith("dl:"))
async def handle_download(call: CallbackQuery, bot: Bot):
    try:
        _, jid, mode, quality = call.data.split(":")
        job = jobs.get(jid)
        if not job:
            await call.answer("أرسل الرابط من جديد.", show_alert=True)
            return
        if job["user_id"] and call.from_user.id != job["user_id"]:
            await call.answer("هذا الطلب لا يخصك.", show_alert=True)
            return

        if download_lock.locked():
            await call.answer("⏳ يوجد تحميل جارٍ الآن. انتظر حتى ينتهي.", show_alert=True)
            return

        await call.answer()
        kind = "audio" if mode == "a" else "video"
        folder = BASE / f"{call.from_user.id}_{uuid.uuid4().hex[:8]}"

        async with download_lock:
            try:
                await call.message.edit_text("⬇️ جاري التحميل...")
                path = await asyncio.wait_for(
                    asyncio.to_thread(download_media, job["url"], kind, quality, folder),
                    timeout=15 * 60,
                )

                size = path.stat().st_size
                if size > MAX_FILE_BYTES:
                    await call.message.edit_text(
                        f"⚠️ حجم الملف {size/1024/1024:.1f} MB ويتجاوز الحد المضبوط "
                        f"({MAX_FILE_MB} MB). جرّب جودة أقل."
                    )
                    return

                await call.message.edit_text("📤 جاري الإرسال...")

                if kind == "audio":
                    await bot.send_audio(
                        call.message.chat.id,
                        FSInputFile(path),
                        caption="⭐ نجم تحميل",
                        request_timeout=120,
                    )
                else:
                    try:
                        await bot.send_video(
                            call.message.chat.id,
                            FSInputFile(path),
                            supports_streaming=True,
                            caption="⭐ نجم تحميل",
                            request_timeout=180,
                        )
                    except Exception:
                        await bot.send_document(
                            call.message.chat.id,
                            FSInputFile(path),
                            caption="⭐ نجم تحميل",
                            request_timeout=180,
                        )

                await call.message.edit_text("✅ تم التحميل والإرسال بنجاح.")
                jobs.pop(jid, None)

            except asyncio.TimeoutError:
                await call.message.edit_text("⏱ العملية أخذت وقتًا طويلًا وتم إيقافها بأمان. أعد المحاولة بجودة أقل.")
            except Exception as e:
                log.exception("download error")
                await call.message.edit_text(
                    "❌ فشل هذا التحميل، لكن البوت ما زال يعمل.\n"
                    f"<code>{str(e)[:220]}</code>"
                )
            finally:
                shutil.rmtree(folder, ignore_errors=True)

    except Exception:
        log.exception("callback handler failure")
        try:
            await call.answer("حدث خطأ مؤقت. أرسل الرابط من جديد.", show_alert=True)
        except Exception:
            pass


async def cleanup_loop():
    while True:
        try:
            purge_old_jobs()
            for p in BASE.iterdir():
                if p.is_dir():
                    try:
                        if time.time() - p.stat().st_mtime > 3600:
                            shutil.rmtree(p, ignore_errors=True)
                    except Exception:
                        pass
        except Exception:
            log.exception("cleanup loop error")
        await asyncio.sleep(900)


async def run_bot_once():
    bot = Bot(BOT_TOKEN, default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    try:
        me = await bot.get_me()
        log.info("Authenticated as @%s", me.username)

        dp = Dispatcher()
        dp.include_router(router)

        cleanup_task = asyncio.create_task(cleanup_loop())
        try:
            log.info("Starting polling...")
            await dp.start_polling(
                bot,
                polling_timeout=30,
                handle_signals=True,
                close_bot_session=False,
            )
        finally:
            cleanup_task.cancel()
    finally:
        await bot.session.close()


async def main():
    if not BOT_TOKEN:
        raise RuntimeError("BOT_TOKEN غير موجود في Railway Variables.")

    # مراقب داخلي: إذا انقطع polling بسبب خطأ شبكي أو خطأ مؤقت،
    # يعيد تشغيله تلقائيًا بدل إنهاء الحاوية.
    while True:
        try:
            await run_bot_once()
        except asyncio.CancelledError:
            raise
        except Exception:
            log.exception("Bot loop crashed; restarting in 10 seconds")
            await asyncio.sleep(10)


if __name__ == "__main__":
    asyncio.run(main())
