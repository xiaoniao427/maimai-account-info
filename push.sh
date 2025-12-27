#!/data/data/com.termux/files/usr/bin/bash
set -e
cd /storage/emulated/0/abc/MaimaiAccountInfo
git add .
git diff-index --quiet HEAD || git commit --allow-empty -m "重构"
git push origin main --force-with-lease
