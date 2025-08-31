import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 10,
  duration: __ENV.DURATION || '1m',
};

const BASE = `${__ENV.BASE_URL || 'http://localhost:8082'}/rest/v1/resultados`;

export default function () {
  let res = http.get(`${BASE}?pagina=0&tamanho=20`);
  check(res, { 'status is 2xx/3xx/401/403': r => [200,201,202,204,301,302,401,403].includes(r.status) });
  sleep(1);
}

