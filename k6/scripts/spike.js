/**
 * Spike Test - Deporturnos API
 * 
 * Objetivo:
 *   Evaluar la estabilidad y capacidad de respuesta del sistema ante un pico repentino de tráfico.
 *   Se simula la llegada súbita de 1500 usuarios concurrentes realizando operaciones de lectura ligeras.
 * 
 * Escenario:
 *   1. Login con cuenta de prueba.
 *   2. Consultas básicas: canchas disponibles, turnos y reservas del usuario.
 * 
 * Notas:
 *   - No se crean ni cancelan reservas, ya que esas operaciones se evalúan en el Stress Test.
 *   - Este test busca detectar degradación del rendimiento, errores y tiempos de respuesta fuera de umbral.
 * 
 * Autor: Joaquin Olivero
 * Fecha: 2025-11-11
 */

import { sleep, check } from "k6";
import { Trend, Rate } from "k6/metrics";
import { login, getCanchasDisponibles, getTurnosDisponiblesDeCancha, getReservasDeUsuario, crearReserva } from "../helpers/actions.js";

// === Custom Metrics ===
export const apiErrorRate = new Rate("api_error_rate");
export const canchasTrend = new Trend("get_canchas_duration");
export const turnosTrend = new Trend("get_turnos_duration");
export const reservasTrend = new Trend("get_reservas_duration");

// === Configuration ===
export let options = {
    stages: [
        { duration: "10s", target: 0 },
        { duration: "15s", target: 1500 },
        { duration: "15s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.02"],
        http_req_duration: ["p(95)<5000"],
        api_error_rate: ["rate<0.02"],
    },
    noConnectionReuse: false,
    discardResponseBodies: true,
};

// === Setup: obtener token con cuenta de prueba ===
export function setup() {
    const loginRes = login("joacolivero.dev@gmail.com", "password123/*");
    if (!loginRes || !loginRes.token) {
        throw new Error(`❌ Login fallido en setup: no se pudo obtener token válido. Resultado: ${JSON.stringify(loginRes)}`);
    }

    console.log("✅ Login exitoso en setup, token recibido.");
    return loginRes;
}

// === Main Execution ===
export default function (data) {
    const resCanchas = getCanchasDisponibles(data.token);
    const resTurnos = getTurnosDisponiblesDeCancha(data.token);
    const resReservas = getReservasDeUsuario(data.token, data.userId);

    canchasTrend.add(resCanchas.timings.duration);
    turnosTrend.add(resTurnos.timings.duration);
    reservasTrend.add(resReservas.timings.duration);

    const okReserva = crearReserva(data.token);
    if (!okReserva) console.log("FAIL CREATING RESERVAS")
    

    check(resCanchas, { "canchas status 200": (r) => r.status === 200 });
    check(resTurnos, { "turnos status 200": (r) => r.status === 200 });
    check(resReservas, { "reservas status 200": (r) => r.status === 200 });

    sleep(0.3 + Math.random() * 0.2);
}
