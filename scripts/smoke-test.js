#!/usr/bin/env node
// Boots server.js against scripts/fake-demo.sh and checks the click-to-run
// protocol: beats only run when the deck asks, in order, one at a time.
const http = require('http');
const { WebSocket } = require('ws');

process.env.PORT = process.env.PORT || '4199';
process.env.DEMO_SCRIPT = './scripts/fake-demo.sh';
require('../server.js');

const base = `http://localhost:${process.env.PORT}`;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const get = (p) => new Promise((ok) => http.get(base + p, (r) => { let b = ''; r.on('data', (d) => (b += d)); r.on('end', () => ok({ code: r.statusCode, body: b })); }).on('error', () => ok({ code: 0, body: '' })));
const assert = (cond, msg) => { if (!cond) throw new Error(msg); };

async function until(fn, ms = 4000) {
  for (let t = 0; t < ms; t += 50) { const v = await fn(); if (v) return v; await sleep(50); }
  throw new Error('timed out waiting');
}

async function main() {
  await sleep(300);
  const ws = new WebSocket(`ws://localhost:${process.env.PORT}/ws`);
  let state = null;
  ws.on('message', (d) => { const m = JSON.parse(String(d)); if (m.type === 'state') state = m.state; });
  await new Promise((r) => ws.on('open', r));

  await until(() => state && state.ready === 1);
  await sleep(400);
  assert(state.beat === 0, 'demo must not run a beat before the deck asks');

  ws.send('goto:2'); // presenter clicked ahead: beats 1 and 2 must both run, in order
  await until(() => state.beat === 2 && state.results[2]);
  assert(state.results[1] && state.results[1].actions === 1, 'beat 1 result missing');
  await sleep(500);
  assert(state.beat === 2, 'demo ran past the slide the deck asked for');

  ws.send('goto:1'); // going back never re-runs or rewinds
  await sleep(300);
  assert(state.beat === 2, 'going back changed the demo');

  ws.send('goto:3');
  await until(() => state.beat === 3 && state.results[3]);

  for (const p of ['/', '/warmer.md', '/dice.md', '/vendor/xterm.js', '/vendor/xterm.css', '/vendor/addon-fit.js', '/fonts/jetbrains-mono-Regular.woff2', '/reveal.js/dist/reveal.js']) {
    const r = await get(p);
    assert(r.code === 200, `asset ${p} -> ${r.code}`);
  }
  assert((await get('/../server.js')).code === 404, 'path traversal must 404');
  const health = JSON.parse((await get('/healthz')).body);
  assert(health.beat === 3, 'healthz out of sync');

  ws.send('restart');
  await until(() => state.beat === 0 && state.ready === 1);

  console.log('SMOKE TEST PASSED');
  process.exit(0);
}

main().catch((e) => { console.error('SMOKE TEST FAILED:', e.message); process.exit(1); });
