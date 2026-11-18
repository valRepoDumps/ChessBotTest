import {request, send} from '../state.js'

export async function savesRequest(type, payload, { okNotice = '', failPrefix = '', extra = {} } = {}) {
  const isList = type === 'list_saves';

  const r = await request(type, payload);
  let ok = r.ok === true;
  let reason = ok ? '' : (r.reason || 'rejected');
  let saves = r.saves;

  if (ok && !Array.isArray(saves)) {
    const l = await request('list_saves');
    if (l.ok === true) saves = l.saves;
    else { ok = false; reason = l.reason || 'no response'; }
  }

  if (ok || isList) {
    send('saves', { ok, reason, saves: ok && Array.isArray(saves) ? saves : [], ...(ok ? extra : {}) });
  }
  
  if (!isList && (okNotice || !ok)) send('notice', ok ? okNotice : `${failPrefix}: ${reason}`);

  return { ok, reason };
}