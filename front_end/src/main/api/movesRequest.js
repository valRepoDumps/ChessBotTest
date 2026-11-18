import {request, send} from '../state.js'

export async function getValidMovesRequest(type, square) {
  const r = await request(type, { square }, 5000);
  return Array.isArray(r.moves) ? r.moves : [];   
}