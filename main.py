import asyncio
import logging
import os
import re
import shutil
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

BASE = Path("/tmp/najm_downloads")
BASE.mkdir(parents=True, exist_ok=True)

logging.basicConfig(level=logging.INFO)
router = Router()
jobs = {}
URL_RE = re.compile(r"https?://[^\s<>()]+", re.I)

def get_url(text):
    m = URL_RE.search(text or "")
    return m.group(0).rstrip(".,،؛;)") if m else None

def base_opts():
    return {
        "quiet": True,
        "no_warnings": True,
        "noplaylist": True,
        "socket_timeout": 30,
        "retries": 3,
    }

def probe(url):
    opts = base_opts()
    opts["skip_download"] = True
    with yt_dlp.YoutubeDL(opts) as ydl:
        info = ydl.extract_info(url, download=False)

    if info and info.get("_type") in {"playlist", "multi_video"}:
        entries = [x for x in (info.get("entries") or []) if x]
        if not entries:
            raise RuntimeError("لم يتم العثور على فيديو.")
        info = entries[0]

    return {
        "title": info.get("title") or "فيديو",
        "url": info.get("webpage_url") or url,
        "extractor": info.get("extractor_key") or info.get("extractor") or "Unknown",
    }

def downloaded_file(folder: Path):
    files = [
        p for p in folder.iterdir()
        if p.is_file() and not p.name.endswith((".part", ".ytdl", ".temp"))
    ]
    if not files:
        raise RuntimeError("لم يتم العثور على الملف الناتج.")
    return max(files, key=lambda p: p.stat().st_mtime)

def download(url, mode, quality, folder):
    folder.mkdir(parents=True, exist_ok=True)
    opts = base_opts()
    opts["outtmpl"] = str(folder / "%(title).120B [%(id)s].%(ext)s")

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
        h = int(quality)
        opts.update({
            "format": (
                f"bestvideo[height<={h}][ext=mp4]+bestaudio[ext=m4a]/"
                f"best[height<={h}][ext=mp4]/"
                f"bestvideo[height<={h}]+bestaudio/"
                f"best[height<={h}]/best"
            ),
            "merge_output_format": "mp4",
        })

    with yt_dlp.YoutubeDL(opts) as ydl:
        ydl.extract_info(url, download=True)

    return downloaded_file(folder)

def buttons(job_id):
    return InlineKeyboardMarkup(inline_keyboard=[
        [
            InlineKeyboardButton(text="📹 360p", callback_data=f"dl:{job_id}:v:360"),
            InlineKeyboardButton(text="📹 720p", callback_data=f"dl:{job_id}:v:720"),
        ],
        [
            InlineKeyboardButton(text="📹 1080p", callback_data=f"dl:{job_id}:v:1080"),
            InlineKeyboardButton(text="🎵 MP3", callback_data=f"dl:{job_id}:a:0"),
        ],
    ])

@router.message(CommandStart())
async def start(message: Message):
    await message.answer(
        "⭐ <b>نجم تحميل</b>\n\n"
        "أرسل رابط الفيديو مباشرة، ثم اختر الجودة أو MP3.\n\n"
        "استخدم البوت فقط للمحتوى الذي تملك حق تنزيله أو لديك إذن بتنزيله."
    )

@router.message(F.text)
async def handle_url(message: Message):
    url = get_url(message.text)
    if not url:
        await message.answer("🔗 أرسل رابط فيديو صحيح.")
        return

    status = await message.answer("🔎 جاري فحص الرابط...")
    try:
        info = await asyncio.to_thread(probe, url)
        jid = uuid.uuid4().hex[:10]
        jobs[jid] = {
            "url": info["url"],
            "user_id": message.from_user.id if message.from_user else 0,
        }
        await status.edit_text(
            f"✅ <b>{info['title']}</b>\n"
            f"🌐 المنصة: <code>{info['extractor']}</code>\n\n"
            "اختر التحميل:",
            reply_markup=buttons(jid),
        )
    except Exception as e:
        logging.exception("probe error")
        await status.edit_text(
            "❌ تعذر قراءة الرابط. قد يكون خاصًا أو محميًا أو غير مدعوم حاليًا.\n"
            f"<code>{str(e)[:300]}</code>"
        )

@router.callback_query(F.data.startswith("dl:"))
async def handle_download(call: CallbackQuery, bot: Bot):
    _, jid, mode, quality = call.data.split(":")
    job = jobs.get(jid)
    if not job:
        await call.answer("أرسل الرابط من جديد.", show_alert=True)
        return
    if job["user_id"] and call.from_user.id != job["user_id"]:
        await call.answer("هذا الرابط يخص مستخدمًا آخر.", show_alert=True)
        return

    await call.answer()
    kind = "audio" if mode == "a" else "video"
    label = "MP3" if kind == "audio" else f"{quality}p"
    folder = BASE / f"{call.from_user.id}_{uuid.uuid4().hex[:8]}"

    try:
        await call.message.edit_text(f"⬇️ جاري تجهيز <b>{label}</b>...")
        path = await asyncio.to_thread(download, job["url"], kind, quality, folder)

        size = path.stat().st_size
        if size > MAX_FILE_BYTES:
            await call.message.edit_text(
                f"⚠️ حجم الملف {size/1024/1024:.1f} MB ويتجاوز الحد المضبوط "
                f"({MAX_FILE_MB} MB). جرّب جودة أقل."
            )
            return

        await call.message.edit_text("📤 جاري الإرسال...")
        if kind == "audio":
            await bot.send_audio(call.message.chat.id, FSInputFile(path), caption="⭐ نجم تحميل")
        else:
            try:
                await bot.send_video(
                    call.message.chat.id,
                    FSInputFile(path),
                    supports_streaming=True,
                    caption="⭐ نجم تحميل",
                )
            except Exception:
                await bot.send_document(call.message.chat.id, FSInputFile(path), caption="⭐ نجم تحميل")

        await call.message.edit_text("✅ تم الإرسال بنجاح.")
        jobs.pop(jid, None)
    except Exception as e:
        logging.exception("download error")
        await call.message.edit_text(
            "❌ تعذر تنزيل الفيديو. جرّب جودة أخرى أو رابطًا عامًا.\n"
            f"<code>{str(e)[:300]}</code>"
        )
    finally:
        shutil.rmtree(folder, ignore_errors=True)

async def main():
    if not BOT_TOKEN:
        raise RuntimeError("ضع BOT_TOKEN في Variables داخل Railway.")
    bot = Bot(BOT_TOKEN, default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    dp = Dispatcher()
    dp.include_router(router)
    await dp.start_polling(bot)

if __name__ == "__main__":
    asyncio.run(main())
