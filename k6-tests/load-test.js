import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { login } from './api/actions.js';

// 📈 Métricas personalizadas
const loginTrend = new Trend('login_duration');
const canchasTrend = new Trend('canchas_duration');
const turnosTrend = new Trend('turnos_duration');
const reservasTrend = new Trend('reservas_duration');
const successRate = new Rate('successful_requests');

// ⚙️ Configuración del escenario
export const options = {
    stages: [
        { duration: '2m', target: 100 }, // ramp-up
        { duration: '10m', target: 100 }, // carga sostenida
        { duration: '2m', target: 0 }, // ramp-down
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'], // menos del 1% fallos
        http_req_duration: ['p(95)<1000'], // 95% debajo de 1s
        successful_requests: ['rate>0.95'],
    },
};

export default function () {
    const BASE_URL = __ENV.BASE_URL || 'http://app:8080';

    group('🔐 Login', function () {
        const start = Date.now();
        const token = login('admin@supreme.com', 'password123/*');
        const end = Date.now();
        loginTrend.add(end - start);
        check(token, { 'login success': (t) => !!t });
        successRate.add(!!token);
        sleep(1);
    });

    const headers = { Authorization: `Bearer ${login('admin@supreme.com', 'password123/*')}` };

    group('⚽ Consultar canchas disponibles', function () {
        const res = http.get(`${BASE_URL}/api/canchas/disponibles/FUTBOL`, { headers });
        canchasTrend.add(res.timings.duration);
        check(res, { 'status 200': (r) => r.status === 200 });
        successRate.add(res.status === 200);
        sleep(0.5);
    });

    group('📅 Consultar turnos disponibles', function () {
        const res = http.get(`${BASE_URL}/api/turnos/disponibles/1/cancha?fecha=2025-09-21`, { headers });
        turnosTrend.add(res.timings.duration);
        check(res, { 'status 200': (r) => r.status === 200 });
        successRate.add(res.status === 200);
        sleep(0.5);
    });

    group('📥 Simular creación de reserva (mock)', function () {
        const payload = JSON.stringify({
            turnoId: 1,
            usuarioId: 1,
        });
        const res = http.post(`${BASE_URL}/api/reservas/mock`, payload, {
            headers: { ...headers, 'Content-Type': 'application/json' },
        });
        reservasTrend.add(res.timings.duration);
        check(res, {
            'status 201 o 200': (r) => r.status === 200 || r.status === 201,
        });
        successRate.add(res.status === 200 || res.status === 201);
        sleep(1);
    });
}

