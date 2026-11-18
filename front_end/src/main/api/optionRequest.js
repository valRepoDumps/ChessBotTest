import {request, send} from '../state.js'

export async function optionRequest(type, payload) {
  const r = await request(type, payload);
  const ok = r.ok === true;
  const reason = ok ? '' : (r.reason || 'rejected');
  if (!ok) send('notice', `Option failed: ${reason}`);
  return { ok, reason };
}