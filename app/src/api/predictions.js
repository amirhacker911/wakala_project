import api from './client';

export async function requestPrediction(payload, token) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  const res = await api.post('/predictions', payload, { headers });
  return res.data;
}
