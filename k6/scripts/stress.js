/**
 * ============================================================
 * Stress Test - Deporturnos API
 * ============================================================
 *
 *  Objetivo:
 *   Evaluar la resiliencia y capacidad de respuesta del sistema
 *   ante condiciones prolongadas de alta concurrencia.
 *
 *  Flujo simulado:
 *   1. ~85% usuarios existentes inician sesión.
 *   2. ~10% nuevos usuarios se registran.
 *   3. ~5% realizan signup + login consecutivo.
 *   4. Todos consultan canchas, turnos, crean y cancelan reservas.
 *
 *   Se agregan pequeñas pausas aleatorias para simular el
 *   comportamiento humano real y tiempos de pensamiento.
 *
 *   Configuración:
 *   - Estrategia: ramping-vus
 *   - Escenario único con flujo completo
 *   - Métricas personalizadas: tiempos y fallos por acción
 *
 *  Autor: Joaquin Olivero
 *  Fecha: 2025-11-11
 * ============================================================
 */

import { sleep, check } from 'k6';
import { Trend, Counter } from "k6/metrics";
import {
  login,
  signup,
  crearReserva,
  cancelarReserva,
  getCanchasDisponibles,
  getTurnosDisponiblesDeCancha,
  getReservasDeUsuario,
} from '../helpers/actions.js';

// === Datos base de usuarios ===
const USERS = [
  { email: 'test1@test.com', password: 'password123/*' },
  { email: 'test2@test.com', password: 'password123/*' },
  { email: 'test3@test.com', password: 'password123/*' },
  { email: 'test4@test.com', password: 'password123/*' },
  { email: 'test5@test.com', password: 'password123/*' },
  { email: 'test6@test.com', password: 'password123/*' },
  { email: 'test7@test.com', password: 'password123/*' },
];

// === Métricas personalizadas ===
export const tLogin = new Trend('login_duration');
export const tSignup = new Trend('signup_duration');
export const tCrear = new Trend('crear_reserva_duration');
export const tCancelar = new Trend('cancelar_reserva_duration');

export const failsLogin = new Counter('login_failed');
export const failsSignup = new Counter('signup_failed');
export const failsCrear = new Counter('crear_reserva_failed');
export const failsCancelar = new Counter('cancelar_reserva_failed');

// === Setup: loguear usuarios base ===
export function setup() {
  const loggedUsers = USERS.map(user => {
    const res = login(user.email, user.password);
    if (res && res.token) {
      return {
        ...user,
        token: res.token,
        userId: res.userId,
      };
    } else {
      console.warn(`⚠️ Falló login para ${user.email}`);
      return null;
    }
  }).filter(u => u !== null);

  console.log(`✅ Logins completados: ${loggedUsers.length} usuarios.`);
  return { users: loggedUsers };
}

// === Helper: genera usuario aleatorio ===
function randomNewUser() {
  const rand = Math.random().toString(36).substring(2, 8);
  return {
    name: `stressUser_${rand}`,
    email: `stress_${rand}@mail.com`,
    password: 'password123/*',
  };
}

// === Flujo principal del stress test ===
export function stressFlow(data) {
  const users = data.users;
  const user = users[Math.floor(Math.random() * users.length)];

  const token = user.token;
  const userId = user.userId;
  try {
    const chance = Math.random();

    // Obtener canchas y turnos
    const resCanchas = getCanchasDisponibles(token);
    const resTurnos = getTurnosDisponiblesDeCancha(token);

    check(resCanchas, { "canchas status 200": (r) => r.status === 200 });
    check(resTurnos, { "turnos status 200": (r) => r.status === 200 });
    sleep(Math.random() * 0.3 + 0.2);


    // Crear reserva
    const startCrear = Date.now();
    const okReserva = crearReserva(token);
    tCrear.add(Date.now() - startCrear);
    if (!okReserva) failsCrear.add(1);

    // Obtener reservas de usuario
    const resResevas = getReservasDeUsuario(token, userId);
    check(resResevas, { "reservas status 200": (r) => r.status === 200 });


    // Signup nuevo usuario
    if (chance < 0.1) {
      const newUser = randomNewUser();
      const start = Date.now();
      const res = signup(newUser.name, newUser.email, newUser.password);
      tSignup.add(Date.now() - start);
      if (!res || res.status !== 201) {
        failsSignup.add(1);
        console.warn(`⚠️ Signup fallido para ${newUser.email}`);
      }
      sleep(Math.random() * 0.8 + 0.2);
    }

      const startCancel = Date.now();
      const okCancel = cancelarReserva(token, userId);
      tCancelar.add(Date.now() - startCancel);
      if (!okCancel) failsCancelar.add(1);

    //const { tokens } = data;

    // if (!tokens || tokens.length === 0) {
    //       console.warn('⚠️ No hay tokens válidos disponibles para el flujo principal.');
    //       return; 
    //   }

    // const token = tokens[Math.floor(Math.random() * tokens.length)];

    // try {
    //   const chance = Math.random();

    // === 10% -> signup (nuevo usuario)
    // if (chance < 0.1) {
    //   const newUser = randomNewUser();
    //   const start = Date.now();
    //   const res = signup(newUser.name, newUser.email, newUser.password);
    //   tSignup.add(Date.now() - start);

    //   if (!res || res.status !== 201) {
    //     failsSignup.add(1);
    //     console.warn(`⚠️ Signup fallido para ${newUser.email}`);
    //   }
    //   sleep(Math.random() * 0.8 + 0.2);
    // }

    // === 5% -> signup + login inmediato (no se puede porque falta verificar)
    // else if (chance >= 0.1 && chance < 0.15) {
    //   const user = randomNewUser();
    //   signup(user.name, user.email, user.password);
    //   sleep(0.2);
    //   const start = Date.now();
    //   const tokenNuevo = login(user.email, user.password);
    //   tLogin.add(Date.now() - start);
    //   if (!tokenNuevo) failsLogin.add(1);
    // }

    // === 85% -> flujo normal con usuarios base
    //   getCanchasDisponibles(token);
    //   getTurnosDisponiblesDeCancha(token);
    //   sleep(Math.random() * 0.3 + 0.2);

    //   const startCrear = Date.now();
    //   const okCrear = crearReserva(token);
    //   tCrear.add(Date.now() - startCrear);
    //   if (!okCrear) failsCrear.add(1);

    //   //getReservasDeUsuario(token);
    //   sleep(Math.random() * 0.4 + 0.2);

    //   const startCancel = Date.now();
    //   //const okCancel = cancelarReserva(token);
    //   tCancelar.add(Date.now() - startCancel);
    //   //if (!okCancel) failsCancelar.add(1);
    // } catch (err) {
    //   console.error('❌ Error general en stressFlow:', err);
    // }

    sleep(Math.random() * 0.8 + 0.5);

  } catch (err) {
    console.error('❌ Error general en stressFlow:', err);
  }
}


// === Configuración del test ===
export const options = {
  scenarios: {
    stress: {
      executor: 'ramping-vus',
      exec: 'stressFlow',
      stages: [
        { duration: '10s', target: 20 },
        // { duration: '3m', target: 200 },
        // { duration: '5m', target: 400 },
        // { duration: '3m', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<4000'],
    login_failed: ['count<10'],
    signup_failed: ['count<10'],
    crear_reserva_failed: ['count<15'],
    cancelar_reserva_failed: ['count<15'],
  },
};
