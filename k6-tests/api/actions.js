import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://app:8080';

export function login(email, password) {
    let loginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
        email: email,
        password: password
    }), { headers: {
            'Content-Type': 'application/json'
        }});

    check(loginRes, {
        'login status 200': (r) => r.status === 200,
        'login contiene token': (r) => r.json('token') !== undefined
    });

    return loginRes.json('token');
}

export function getCanchasDisponibles(token) {
    let headers = { 'Authorization': `Bearer ${token}` };
    let res = http.get(`${BASE_URL}/api/canchas/disponibles/FUTBOL`, { headers });
    check(res, { 'canchas status 200': (r) => r.status === 200 });
}

export function getTurnosDisponiblesDeCancha(token) {
    let headers = { 'Authorization': `Bearer ${token}` };
    let res = http.get(`${BASE_URL}/api/turnos/disponibles/1/cancha?fecha=2025-09-21`, { headers });
    check(res, { 'canchas status 200': (r) => r.status === 200 });
}