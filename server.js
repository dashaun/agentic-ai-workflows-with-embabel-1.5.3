#!/usr/bin/env node
// Serves the deck from docs/ and runs demo/demo.sh in a PTY, bridged to the
// browser over a WebSocket. The deck drives the demo: arriving on a slide with
// data-beat="N" asks for beat N, and the demo runs each beat in order, one at
// a time. Adapted from the usaa-ai-summit executive deck.
const http = require('http');
const fs = require('fs');
const path = require('path');
const { WebSocketServer } = require('ws');
const pty = require('node-pty');

const ROOT = __dirname;
const DOCS = path.join(ROOT, 'docs');
const PORT = Number(process.env.PORT || 4173);
const DEMO_SCRIPT = process.env.DEMO_SCRIPT || './demo/demo.sh';

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.md': 'text/markdown; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.webp': 'image/webp',
  '.woff2': 'font/woff2',
  '.txt': 'text/plain; charset=utf-8',
};

// Client libraries come straight from node_modules, so the deck stays offline.
const VENDOR = {
  '/vendor/xterm.js': 'node_modules/@xterm/xterm/lib/xterm.js',
  '/vendor/xterm.css': 'node_modules/@xterm/xterm/css/xterm.css',
  '/vendor/addon-fit.js': 'node_modules/@xterm/addon-fit/lib/addon-fit.js',
};

const server = http.createServer((req, res) => {
  const urlPath = decodeURIComponent((req.url || '/').split('?')[0]);
  if (urlPath === '/healthz') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify(publicState()));
  }
  if (VENDOR[urlPath]) return sendFile(res, path.join(ROOT, VENDOR[urlPath]));
  const file = path.normalize(path.join(DOCS, urlPath === '/' ? 'index.html' : urlPath));
  if (!file.startsWith(DOCS)) return send404(res);
  sendFile(res, file);
});

function sendFile(res, file) {
  fs.readFile(file, (err, data) => {
    if (err) return send404(res);
    res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' });
    res.end(data);
  });
}

function send404(res) {
  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('not found');
}

// ---------------------------------------------------------------------------
// The demo PTY. demo.sh announces its progress with private OSC sequences that
// xterm.js ignores:   ESC ] 777 ; ready ; N BEL   waiting to run beat N
//                     ESC ] 777 ; beat ; N BEL    running beat N
//                     ESC ] 777 ; result ; JSON BEL   outcome of the beat
// ---------------------------------------------------------------------------
const clients = new Set();
let demo = null;
let state = freshState();

function freshState() {
  return { alive: false, ready: 0, beat: 0, target: 0, results: {}, exitCode: null };
}

function publicState() {
  const { alive, ready, beat, target, results, exitCode } = state;
  return { alive, ready, beat, target, results, exitCode };
}

function broadcast(obj) {
  const s = JSON.stringify(obj);
  for (const c of clients) if (c.readyState === 1) c.send(s);
}

let history = '';
let carry = '';
const OSC = /\x1b\]777;([a-z]+);?([^\x07]*)\x07/g;

function onData(data) {
  history += data;
  if (history.length > 400000) history = history.slice(-250000);
  broadcast({ type: 'term', data });

  const hay = carry + data;
  let m;
  let changed = false;
  OSC.lastIndex = 0;
  while ((m = OSC.exec(hay))) {
    const [, kind, arg] = m;
    if (kind === 'ready') state.ready = Number(arg);
    if (kind === 'beat') state.beat = Number(arg);
    if (kind === 'result') {
      try { state.results[state.beat] = JSON.parse(arg); } catch (_) { /* ignore malformed */ }
    }
    changed = true;
  }
  // keep an unterminated OSC tail for the next chunk
  const open = hay.lastIndexOf('\x1b]777;');
  carry = open >= 0 && hay.indexOf('\x07', open) < 0 ? hay.slice(open) : '';
  if (changed) {
    broadcast({ type: 'state', state: publicState() });
    advanceIfAsked();
  }
}

// Press "any key" for the demo only when it is waiting for a beat the deck has
// already reached. Fast clicking queues up: each beat runs to completion first.
function advanceIfAsked() {
  if (demo && state.alive && state.ready > state.beat && state.ready <= state.target) {
    state.beat = state.ready; // optimistic: avoid double keypresses before the OSC arrives
    demo.write('\r');
  }
}

function startDemo() {
  if (demo) {
    try { demo.kill(); } catch (_) { /* already gone */ }
  }
  state = freshState();
  history = '';
  carry = '';
  const shell = process.env.SHELL || '/bin/bash';
  demo = pty.spawn(shell, ['-lc', `cd ${JSON.stringify(ROOT)} && exec ${DEMO_SCRIPT}`], {
    name: 'xterm-256color',
    cols: 110,
    rows: 28,
    cwd: ROOT,
    env: process.env,
  });
  state.alive = true;
  const me = demo;
  demo.onData(onData);
  demo.onExit(({ exitCode }) => {
    if (demo !== me) return; // a restart replaced this process
    state.alive = false;
    state.exitCode = exitCode;
    broadcast({ type: 'state', state: publicState() });
  });
  broadcast({ type: 'reset' });
  broadcast({ type: 'state', state: publicState() });
}

const wss = new WebSocketServer({ server, path: '/ws' });
wss.on('connection', (ws) => {
  clients.add(ws);
  ws.send(JSON.stringify({ type: 'state', state: publicState() }));
  if (history) ws.send(JSON.stringify({ type: 'term', data: history }));
  ws.on('message', (raw) => {
    const msg = String(raw);
    if (msg.startsWith('goto:')) {
      const n = Number(msg.slice(5));
      if (n > state.target) {
        state.target = n;
        broadcast({ type: 'state', state: publicState() });
        advanceIfAsked();
      }
    } else if (msg === 'restart') {
      startDemo();
    } else if (msg.startsWith('resize:')) {
      const [cols, rows] = msg.slice(7).split(',').map(Number);
      if (demo && cols > 0 && rows > 0) {
        try { demo.resize(cols, rows); } catch (_) { /* noop */ }
      }
    }
  });
  ws.on('close', () => clients.delete(ws));
});

server.listen(PORT, () => {
  console.log(`\n  Agentic AI Workflows with Embabel: live deck`);
  console.log(`  → http://localhost:${PORT}   (S for speaker view)\n`);
  if (process.env.DEMO_AUTOSTART !== 'false') startDemo();
});

module.exports = { server, startDemo, state: () => state };
