#!/usr/bin/env bash
# Starts the live deck: the slides from docs/ plus the demo terminal.
# Click through the deck; each demo step runs when you reach its slide.
set -euo pipefail
cd "$(dirname "$0")"

if [[ ! -d node_modules ]]; then
  echo "Installing the deck's npm dependencies (one time)..."
  npm install --no-audit --no-fund
fi
# node-pty's spawn-helper needs the exec bit; npm may skip the postinstall.
for h in node_modules/node-pty/prebuilds/*/spawn-helper; do [[ -f "$h" ]] && chmod +x "$h"; done

PORT="${PORT:-4173}"
node server.js &
SERVER_PID=$!
trap 'kill $SERVER_PID 2>/dev/null' EXIT
sleep 1
open "http://localhost:${PORT}" 2>/dev/null || xdg-open "http://localhost:${PORT}" >/dev/null 2>&1 || true
wait $SERVER_PID
