'use strict';
const protocolVersion = 4;
const canvas = document.querySelector('#map');
const ctx = canvas.getContext('2d');
const status = document.querySelector('#status');
const joinButton = document.querySelector('#join');
const leaveButton = document.querySelector('#leave');
let words = {}, map, session, snapshot, sequence = 0, keys = new Set(), generation = 0;
let inputPending = false, statePending = false, lastReceived = 0;
const t = key => words[key] || key;
function message(key) { status.textContent = t(key); }
async function api(path, body, token = session?.token) {
  const options = { headers: {}, signal: AbortSignal.timeout(3000), cache: 'no-store' };
  if (path === '/join') options.headers['X-T4C-Protocol'] = String(protocolVersion);
  if (token) options.headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) {
    options.method = 'POST'; options.body = body;
    options.headers['Content-Type'] = 'text/plain;charset=UTF-8';
  }
  const response = await fetch(path, options);
  const data = await response.json();
  if (!response.ok) throw new Error(data.error || 'connection');
  return data;
}
function reset(key) {
  session = undefined; snapshot = undefined; keys.clear(); generation++;
  joinButton.disabled = false; leaveButton.disabled = true;
  message(key); document.querySelector('#identity').textContent = t('notJoined');
  document.querySelector('#telemetry').textContent = ''; draw();
}
function failed(error) { reset(Object.hasOwn(words, error.message) ? error.message : 'connection'); }
joinButton.addEventListener('click', async () => {
  joinButton.disabled = true; message('connecting');
  try {
    if (map?.protocolVersion !== protocolVersion) throw new Error('connection');
    session = await api('/join', ''); generation++; sequence = 0; lastReceived = performance.now();
    leaveButton.disabled = false; message('connected'); canvas.focus({preventScroll: true});
    document.querySelector('#identity').textContent = `${t('you')} ${session.id}`;
  } catch (error) { failed(error); }
});
leaveButton.addEventListener('click', async () => {
  const token = session?.token; reset('disconnected');
  if (token) { try { await api('/leave', '', token); } catch { /* Session expires on the server. */ } }
});
const movementKeys = new Set(['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'KeyW', 'KeyA', 'KeyS', 'KeyD']);
window.addEventListener('keydown', event => {
  if (!session || !movementKeys.has(event.code)) return;
  event.preventDefault(); keys.add(event.code);
});
window.addEventListener('keyup', event => { if (movementKeys.has(event.code)) { event.preventDefault(); keys.delete(event.code); } });
window.addEventListener('blur', () => keys.clear());
document.addEventListener('visibilitychange', () => { if (document.hidden) keys.clear(); });
window.addEventListener('pagehide', () => {
  if (session) fetch('/leave', {method: 'POST', body: '', keepalive: true,
    headers: {'Content-Type': 'text/plain', Authorization: `Bearer ${session.token}`}}).catch(() => {});
});
setInterval(async () => {
  if (!session || inputPending) return;
  const current = generation; inputPending = true;
  const dx = Number(keys.has('ArrowRight') || keys.has('KeyD')) - Number(keys.has('ArrowLeft') || keys.has('KeyA'));
  const dy = Number(keys.has('ArrowDown') || keys.has('KeyS')) - Number(keys.has('ArrowUp') || keys.has('KeyW'));
  try { await api('/input', `${session.region.locationEpoch} ${sequence++} ${dx} ${dy}`); }
  catch (error) { if (current === generation) failed(error); }
  finally { inputPending = false; }
}, 66);
setInterval(async () => {
  if (!session || statePending) return;
  const current = generation; statePending = true;
  const sent = performance.now();
  try {
    const state = await api('/state');
    if (current !== generation) return;
    snapshot = state; lastReceived = performance.now();
    document.querySelector('#telemetry').textContent = `${t('players')} ${state.players.length}/${map.maxPlayers} · ${t('tick')} ${state.tick} · ${Math.round(lastReceived - sent)} ms`;
    draw();
  } catch (error) { if (current === generation) failed(error); }
  finally { statePending = false; }
}, 100);
setInterval(() => { if (session && performance.now() - lastReceived > 4000) reset('connection'); }, 1000);
function draw() {
  if (!map) return;
  const sx = canvas.width / map.width, sy = canvas.height / map.height;
  ctx.fillStyle = '#182326'; ctx.fillRect(0, 0, canvas.width, canvas.height);
  for (let y = 0; y < map.height; y++) for (let x = 0; x < map.width; x++) {
    ctx.fillStyle = map.blocked[y][x] ? '#556260' : '#20332c';
    ctx.fillRect(x * sx + .5, y * sy + .5, sx - 1, sy - 1);
  }
  for (const player of snapshot?.players || []) {
    const self = player.id === session?.id;
    ctx.beginPath(); ctx.arc(player.x * sx, player.y * sy, self ? 10 : 9, 0, Math.PI * 2);
    ctx.fillStyle = self ? '#f6d185' : '#82c9ef'; ctx.fill();
    ctx.strokeStyle = '#11191c'; ctx.lineWidth = 2; ctx.stroke();
    ctx.fillStyle = '#ffffff'; ctx.font = 'bold 18px system-ui'; ctx.textAlign = 'center';
    ctx.fillText(String(player.id), player.x * sx, player.y * sy - 16);
  }
}
async function initialize() {
  try {
    [words, map] = await Promise.all([api('/strings'), api('/map')]);
    document.title = t('title');
    document.querySelectorAll('[data-i18n]').forEach(element => { element.textContent = t(element.dataset.i18n); });
    canvas.setAttribute('aria-label', t('mapLabel')); reset('ready');
  } catch { message('connection'); }
}
initialize();
