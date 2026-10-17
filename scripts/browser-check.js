#!/usr/bin/env node
// Drives the cached headless Chromium over the DevTools protocol: opens the
// deck, presses the right arrow like a clicker, and screenshots each stop.
// Usage: node scripts/browser-check.js <url> <outDir> <presses> [waitMs]
const { spawn } = require('child_process');
const fs = require('fs');
const http = require('http');
const WebSocket = require('ws');

const [url, outDir, presses = '0', waitMs = '2500'] = process.argv.slice(2);
const CHROME = process.env.CHROME || `${process.env.HOME}/Library/Caches/ms-playwright/chromium_headless_shell-1234/chrome-headless-shell-mac-arm64/chrome-headless-shell`;
const port = 9333;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

(async () => {
  const chrome = spawn(CHROME, ['--headless', `--remote-debugging-port=${port}`, '--window-size=1600,900', '--hide-scrollbars', 'about:blank'], { stdio: 'ignore' });
  let target;
  for (let i = 0; i < 50 && !target; i++) {
    await sleep(100);
    target = await new Promise((ok) => http.get(`http://127.0.0.1:${port}/json`, (r) => { let b = ''; r.on('data', (d) => (b += d)); r.on('end', () => { try { ok(JSON.parse(b).find((t) => t.type === 'page')); } catch (_) { ok(null); } }); }).on('error', () => ok(null)));
  }
  const ws = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((r) => ws.on('open', r));
  let id = 0; const waiting = new Map();
  ws.on('message', (d) => { const m = JSON.parse(String(d)); if (m.id && waiting.has(m.id)) { waiting.get(m.id)(m.result); waiting.delete(m.id); } });
  const cmd = (method, params = {}) => new Promise((ok) => { const n = ++id; waiting.set(n, ok); ws.send(JSON.stringify({ id: n, method, params })); });
  const evaluate = async (expr) => (await cmd('Runtime.evaluate', { expression: expr, returnByValue: true })).result.value;
  const shot = async (name) => { const r = await cmd('Page.captureScreenshot', { format: 'png' }); fs.writeFileSync(`${outDir}/${name}.png`, Buffer.from(r.data, 'base64')); };
  const press = async () => {
    for (const type of ['keyDown', 'keyUp']) await cmd('Input.dispatchKeyEvent', { type, key: 'ArrowRight', code: 'ArrowRight', windowsVirtualKeyCode: 39 });
  };
  await cmd('Page.enable'); await cmd('Runtime.enable');
  await cmd('Page.navigate', { url });
  await sleep(Number(waitMs));
  for (let i = 0; i <= Number(presses); i++) {
    if (i > 0) { await press(); await sleep(Number(waitMs)); }
    const info = await evaluate(`JSON.stringify({ live: document.body.classList.contains('live-on'), slide: Reveal.getIndices(), title: (document.getElementById('live-title')||{}).textContent, outcome: (document.getElementById('live-outcome')||{}).textContent })`);
    console.log(i, info);
    await shot(`stop-${String(i).padStart(2, '0')}`);
  }
  ws.close(); chrome.kill();
  process.exit(0);
})().catch((e) => { console.error(e); process.exit(1); });
