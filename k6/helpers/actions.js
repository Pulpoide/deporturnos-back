import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from './config.js';

// === helper para headers ===
function authHeaders(token) {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

function httpGetWithRetry(url, headers, maxAttempts = 3, waitMs = 200) {
    let res;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        res = http.get(url, { headers, tags: { endpoint: url } });

        if (res.status && res.status < 500 && res.status !== 0) return res;

        console.warn(`⚠️ [GET intento ${attempt}] status=${res.status} en ${url}`);
        sleep(waitMs / 1000);
        waitMs *= 2;
    }

    console.error(`❌ GET falló luego de ${maxAttempts} intentos: ${url}`);
    return res;
}

function httpPostWithRetry(url, payload, headers = {}, maxAttempts = 3, waitMs = 200) {
    let res;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            res = http.post(url, payload, {
                headers,
                tags: { endpoint: url },
                responseType: 'text',
            });

            if (!res || typeof res !== 'object') {
                console.warn(`⚠️ [POST intento ${attempt}] Respuesta nula o inválida para ${url}`);
            } else if (res.status && res.status < 500 && res.status !== 0) {
                return res;
            } else {
                console.warn(`⚠️ [POST intento ${attempt}] status=${res.status} en ${url}`);
            }
        } catch (err) {
            console.error(`❌ [POST intento ${attempt}] Excepción en ${url}: ${err.message}`);
        }

        sleep(waitMs / 1000);
        waitMs *= 2; 
    }

    console.error(`❌ POST falló luego de ${maxAttempts} intentos: ${url}`);
    return null;
}

function httpPutWithRetry(url, payload, headers, maxAttempts = 3, waitMs = 200) {
    let res;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            res = http.put(url, payload, { headers, tags: { endpoint: url } });

            if (res.status && res.status < 500 && res.status !== 0) return res;

            console.warn(`⚠️ [PUT intento ${attempt}] status=${res.status} en ${url}`);
        } catch (err) {
            console.error(`❌ [PUT intento ${attempt}] Excepción en ${url}: ${err.message}`);
        }

        sleep(waitMs / 1000);
        waitMs *= 2;
    }

    console.error(`❌ PUT falló luego de ${maxAttempts} intentos: ${url}`);
    return res;
}


export function signup(name, email, password) {
    const payload = JSON.stringify({ nombre: name, email, password });
    const res = httpPostWithRetry(`${BASE_URL}/api/auth/signup`, payload, { 'Content-Type': 'application/json' });

    check(res, {
        'signup status 201': (r) => r.status === 201,
        'signup contiene id': (r) => r.json('id') !== undefined,
    });

    return res;
}

export function login(email, password) {
    const payload = JSON.stringify({ email, password });
    const res = httpPostWithRetry(`${BASE_URL}/api/auth/login`, payload, {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    });
    if (!res || res.status !== 200 || !res.body) {
        console.warn(`⚠️ Login fallido (status: ${res ? res.status : 'sin respuesta'}) para ${email}`);
        return null;
    }

    let token = null;
    let userId = null;
    try {
        const data = JSON.parse(res.body);
        token = data.token;
        userId = data.id;
    } catch (e) {
        console.warn(`⚠️ Error parseando token de login (${res.status}):`, e);
        console.log(`🔍 login body recibido: ${res.body.substring(0, 200)}`);
    }

    check(res, {
        'login status 200': (r) => r.status === 200,
        'login contiene token': () => token !== null && token !== undefined,
        'login contiene id': () => userId !== null && userId !== undefined,
    });

    return { token, userId };
}


export function getCanchasDisponibles(token) {
    const url = `${BASE_URL}/api/canchas/disponibles/FUTBOL`;
    const res = httpGetWithRetry(url, authHeaders(token));

    check(res, {
        "canchas status 200": (r) => r && r.status === 200,
    }) || console.warn(`🔍⚠️ canchas status: ${res.status}`);

    sleep(0.2 + Math.random() * 0.2);
    return res;
}

export function getTurnosDisponiblesDeCancha(token) {
    const canchaId = Math.floor(Math.random() * 4) + 1;
    const url = `${BASE_URL}/api/turnos/disponibles/${canchaId}/cancha?fecha=2025-10-29`;
    const res = httpGetWithRetry(url, authHeaders(token));

    check(res, {
        "turnos status 200": (r) => r && r.status === 200,
    }) || console.warn(`🔍⚠️ turnos status: ${res.status}`);

    sleep(0.2 + Math.random() * 0.2);
    return res;
}

export function getReservasDeUsuario(token, userId) {
    const url = `${BASE_URL}/api/usuarios/${userId}/reservas`;
    const res = httpGetWithRetry(url, authHeaders(token));

    check(res, {
        "reservas status 200": (r) => r && r.status === 200,
    }) || console.warn(`🔍⚠️ reservas status: ${res.status}`);

    sleep(0.2 + Math.random() * 0.2);
    return res;
}


export function crearReserva(token) {
    const canchaId = Math.floor(Math.random() * 4) + 1;

    const fechaInicio = new Date('2025-11-11');
    const fechaFin = new Date('2026-12-31');
    const fechaRandom = new Date(
        fechaInicio.getTime() +
        Math.random() * (fechaFin.getTime() - fechaInicio.getTime())
    );
    const fechaFormateada = fechaRandom.toISOString().split('T')[0];

    const urlGetTurnos = `${BASE_URL}/api/turnos/disponibles/${canchaId}/cancha?fecha=${fechaFormateada}`;
    const resTurnos = httpGetWithRetry(urlGetTurnos, authHeaders(token));

    check(resTurnos, { 'turnos disponibles OK': (r) => r.status === 200 });

    let turnosDisponibles = [];
    try {
        turnosDisponibles = JSON.parse(resTurnos.body);
    } catch (e) {
        console.warn('⚠️ No se pudo parsear respuesta de turnos:', e);
    }

    if (!turnosDisponibles || turnosDisponibles.length === 0) {
        console.warn(`⚠️ No hay turnos disponibles para cancha ${canchaId} en ${fechaFormateada}`);
        sleep(1);
        return false;
    }

    const turnoRandom = turnosDisponibles[Math.floor(Math.random() * turnosDisponibles.length)];
    const turnoId = JSON.stringify({ turnoId: turnoRandom.id });

    const urlPostReserva = `${BASE_URL}/api/reservas/byuser`;
    const resReserva = httpPostWithRetry(urlPostReserva, turnoId, authHeaders(token));
    check(resReserva, {
        'reserva creada correctamente': (r) => [200, 400, 409].includes(r.status),
    });

    sleep(0.5);
    return resReserva.status === 200;
}

export function cancelarReserva(token, userId) {
    const urlGetReservas = `${BASE_URL}/api/usuarios/${userId}/reservas`;
    const resReservas = httpGetWithRetry(urlGetReservas, authHeaders(token));


    const ok = check(resReservas, { 'reservas obtenidas status 200': (r) => r.status === 200 });
    if (!ok) {
        console.warn(`⚠️ No se pudieron obtener reservas para usuario ${userId} (status ${resReservas.status})`);
        sleep(0.5);
        return;
    }

    let reservas = [];
    try {
        reservas = JSON.parse(resReservas.body);
    } catch (e) {
        console.warn(`⚠️ Error parseando reservas del usuario ${userId}:`, e);
        sleep(0.5);
        return;
    }

    if (!reservas || reservas.length === 0) {
        console.log(`ℹ️ Usuario ${userId} no tiene reservas.`);
        sleep(0.5);
        return;
    }

    const activas = reservas.filter((r) => r.estado === 'CONFIRMADA');
    if (activas.length === 0) {
        console.log(`ℹ️ Usuario ${userId} no tiene reservas activas.`);
        sleep(0.5);
        return;
    }

    const randomReserva = activas[Math.floor(Math.random() * activas.length)];

    const urlCancelRes = `${BASE_URL}/api/reservas/${randomReserva.id}/cancelar`;
    const cancelRes = httpPutWithRetry(urlCancelRes, authHeaders(token));

    check(cancelRes, {
        'reserva cancelada correctamente': (r) => r.status === 200,
    });

    if (cancelRes.status !== 200) {
        console.warn(`⚠️ Falló la cancelación de reserva ${randomReserva.id} (status ${cancelRes.status})`);
    }

    sleep(0.5);
    return cancelRes.status === 200;
}