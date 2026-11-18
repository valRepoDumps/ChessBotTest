import {request, send} from '../state.js'

export async function listEngineOptions(type, payload){
  const r = await request(type, { advanced: true, ...payload });
  const ok = r.ok === true;   // "No engine selected" arrives here as a failure -> nothing is shown
  return {
    ok,
    reason: ok ? '' : (r.reason || 'no response'),
    presets: ok && Array.isArray(r.preset_options) ? r.preset_options : [],
    advanced: ok && Array.isArray(r.advanced_options) ? r.advanced_options : [],
  };
}

export async function listEngines(type){
  const r = await request(type, {}, 30000);   // the backend starts each candidate to check it
  const ok = r.ok !== false;
  return { ok, reason: ok ? '' : (r.reason || 'no response'),
          engines: ok && Array.isArray(r.engines) ? r.engines : [] };
}