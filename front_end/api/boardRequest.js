import {request, send} from '../state.js'

export async function boardRequest(type, payload, { okNotice = '', failPrefix, timeoutMs }) {

  const r = await request(type, payload, timeoutMs);
  const ok = r.ok === true && typeof r.fen === 'string';
  const reason = ok ? '' : (r.reason || 'rejected');

  if (ok) send('update_board', r);                       // forward everything

  if (ok ? okNotice : true) send('notice', ok ? okNotice : `${failPrefix}: ${reason}`);

  return { ok, reason };
}

