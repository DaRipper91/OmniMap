#!/bin/bash
# Continuum Native Runner
PORT=5173
echo "🌌 Starting Continuum Intelligence Engine..."
# Navigate to the installed lib directory
cd /usr/lib/continuum
# Start a simple static server for the dist folder
npx -y serve -s . -l $PORT &
sleep 2
# Open in "App Mode" (Chromium/Chrome/Firefox)
if command -v google-chrome-stable &> /dev/null; then
    google-chrome-stable --app=http://localhost:$PORT
elif command -v chromium &> /dev/null; then
    chromium --app=http://localhost:$PORT
else
    xdg-open http://localhost:$PORT
fi
