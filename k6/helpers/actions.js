import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from './config.js';

// === Helper for headers ===
function authHeaders(token) {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
}

// === Retry Helpers ===
function httpGetWithRetry(url, headers, maxAttempts = 3, waitMs = 200) {
    let res;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        res = http.get(url, { headers, tags: { endpoint: url } });

        if (res.status && res.status < 500 && res.status !== 0) return res;

        console.warn(`⚠️ [GET attempt ${attempt}] status=${res.status} at ${url}`);
        sleep(waitMs / 1000);
        waitMs *= 2;
    }

    console.error(`❌ GET failed after ${maxAttempts} attempts: ${url}`);
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
                console.warn(`⚠️ [POST attempt ${attempt}] Invalid response for ${url}`);
            } else if (res.status && res.status < 500 && res.status !== 0) {
                return res;
            } else {
                console.warn(`⚠️ [POST attempt ${attempt}] status=${res.status} at ${url}`);
            }
        } catch (err) {
            console.error(`❌ [POST attempt ${attempt}] Exception at ${url}: ${err.message}`);
        }

        sleep(waitMs / 1000);
        waitMs *= 2; 
    }

    console.error(`❌ POST failed after ${maxAttempts} attempts: ${url}`);
    return null;
}

function httpPutWithRetry(url, payload, headers, maxAttempts = 3, waitMs = 200) {
    let res;
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            res = http.put(url, payload, { headers, tags: { endpoint: url } });

            if (res.status && res.status < 500 && res.status !== 0) return res;

            console.warn(`⚠️ [PUT attempt ${attempt}] status=${res.status} at ${url}`);
        } catch (err) {
            console.error(`❌ [PUT attempt ${attempt}] Exception at ${url}: ${err.message}`);
        }

        sleep(waitMs / 1000);
        waitMs *= 2;
    }

    console.error(`❌ PUT failed after ${maxAttempts} attempts: ${url}`);
    return res;
}


// === Actions ===

export function signup(name, email, password) {
    const payload = JSON.stringify({ nombre: name, email, password });
    const res = httpPostWithRetry(`${BASE_URL}/api/auth/signup`, payload, { 'Content-Type': 'application/json' });

    check(res, {
        'signup status 201': (r) => r.status === 201,
        'signup has id': (r) => r.json('id') !== undefined,
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
        // console.warn(`⚠️ Login failed (status: ${res ? res.status : 'no response'}) for ${email}`);
        return null;
    }

    let token = null;
    let userId = null;
    try {
        const data = JSON.parse(res.body);
        token = data.token;
        userId = data.id;
    } catch (e) {
        console.warn(`⚠️ Error parsing login token (${res.status}):`, e);
    }

    check(res, {
        'login status 200': (r) => r.status === 200,
        'login has token': () => token !== null && token !== undefined,
        'login has id': () => userId !== null && userId !== undefined,
    });

    return { token, userId };
}

/**
 * Attempts to login with the provided credentials.
 * If login fails, attempts to signup and then login again.
 * @param {string} email 
 * @param {string} password 
 * @param {string} name (Optional) Name to use if signup is needed
 * @returns {Object|null} Object containing token and userId, or null if failed.
 */
export function ensureAuth(email, password, name = "Test User") {
    let auth = login(email, password);
    
    if (auth && auth.token) {
        return auth;
    }

    console.log(`ℹ️ Login failed for ${email}, attempting signup...`);
    const signupRes = signup(name, email, password);

    if (signupRes && signupRes.status === 201) {
        console.log(`✅ Signup successful for ${email}, retrying login...`);
        sleep(0.5); // wait a bit before login
        auth = login(email, password);
        if (auth && auth.token) {
            return auth;
        }
    } else {
        console.warn(`⚠️ Signup failed for ${email}`);
    }

    return null;
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
    // Using a fixed date or dynamic date? Better dynamic to ensure availability if backend validates dates
    const today = new Date().toISOString().split('T')[0];
    const url = `${BASE_URL}/api/turnos/disponibles/${canchaId}/cancha?fecha=${today}`;
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

    // Pick a random future date
    const fechaInicio = new Date();
    fechaInicio.setDate(fechaInicio.getDate() + 1); // tomorrow
    const fechaFin = new Date();
    fechaFin.setDate(fechaFin.getDate() + 30); // next 30 days
    
    const fechaRandom = new Date(
        fechaInicio.getTime() +
        Math.random() * (fechaFin.getTime() - fechaInicio.getTime())
    );
    const fechaFormateada = fechaRandom.toISOString().split('T')[0];

    const urlGetTurnos = `${BASE_URL}/api/turnos/disponibles/${canchaId}/cancha?fecha=${fechaFormateada}`;
    const resTurnos = httpGetWithRetry(urlGetTurnos, authHeaders(token));

    if (!resTurnos || resTurnos.status !== 200) return false;

    let turnosDisponibles = [];
    try {
        turnosDisponibles = JSON.parse(resTurnos.body);
    } catch (e) {
        console.warn('⚠️ Could not parse turnos response:', e);
    }

    if (!turnosDisponibles || !Array.isArray(turnosDisponibles) || turnosDisponibles.length === 0) {
        // console.warn(`⚠️ No available turns for cancha ${canchaId} on ${fechaFormateada}`);
        sleep(0.5);
        return false;
    }

    const turnoRandom = turnosDisponibles[Math.floor(Math.random() * turnosDisponibles.length)];
    const payload = JSON.stringify({ turnoId: turnoRandom.id });

    const urlPostReserva = `${BASE_URL}/api/reservas/byuser`;
    const resReserva = httpPostWithRetry(urlPostReserva, payload, authHeaders(token));
    
    const success = check(resReserva, {
        'reserva created or conflict': (r) => [200, 400, 409].includes(r.status),
    });

    sleep(0.5);
    return resReserva && resReserva.status === 200;
}

export function cancelarReserva(token, userId) {
    const urlGetReservas = `${BASE_URL}/api/usuarios/${userId}/reservas`;
    const resReservas = httpGetWithRetry(urlGetReservas, authHeaders(token));

    const ok = check(resReservas, { 'reservas fetched status 200': (r) => r.status === 200 });
    if (!ok) {
        sleep(0.5);
        return false;
    }

    let reservas = [];
    try {
        const body = JSON.parse(resReservas.body);
        // Handle pagination: if body has 'content', use it; otherwise assume it's the list (legacy)
        reservas = Array.isArray(body) ? body : (body.content || []);
    } catch (e) {
        console.warn(`⚠️ Error parsing reserves for user ${userId}:`, e);
        sleep(0.5);
        return false;
    }

    if (!reservas || reservas.length === 0) {
        // console.log(`ℹ️ User ${userId} has no reserves.`);
        sleep(0.5);
        return true; // nothing to cancel is technically a success of the flow
    }

    const activas = reservas.filter((r) => r.estado === 'CONFIRMADA' || r.estado === 'PENDIENTE'); // Assuming PENDIENTE might exist
    if (activas.length === 0) {
        sleep(0.5);
        return true;
    }

    const randomReserva = activas[Math.floor(Math.random() * activas.length)];

    const urlCancelRes = `${BASE_URL}/api/reservas/${randomReserva.id}/cancelar`;
    const cancelRes = httpPutWithRetry(urlCancelRes, null, authHeaders(token)); // null payload for PUT if no body needed

    check(cancelRes, {
        'reserva cancelled correctly': (r) => r.status === 200,
    });

    sleep(0.5);
    return cancelRes && cancelRes.status === 200;
}
