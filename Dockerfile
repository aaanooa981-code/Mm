FROM python:3.12-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --no-cache-dir "aiogram>=3.10,<4" yt-dlp

WORKDIR /app
COPY main.py /app/main.py

CMD ["python", "-u", "main.py"]
