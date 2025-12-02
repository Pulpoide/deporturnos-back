import { sleep, check } from 'k6';
import { Trend, Counter } from "k6/metrics";
import {
  signup,
  crearReserva,
  cancelarReserva,
  getCanchasDisponibles,
  getTurnosDisponiblesDeCancha,
  getReservasDeUsuario,
} from '../helpers/actions.js';
import { randomName, randomEmail, randomPassword } from '../helpers/utils.js';

// === Custom Metrics ===
export const tLogin = new Trend('login_duration');
export const tSignup = new Trend('signup_duration');
export const tCrear = new Trend('crear_reserva_duration');
export const tCancelar = new Trend('cancelar_reserva_duration');

export const failsLogin = new Counter('login_failed');
export const failsSignup = new Counter('signup_failed');
export const failsCrear = new Counter('crear_reserva_failed');
export const failsCancelar = new Counter('cancelar_reserva_failed');

// === Customer Flow Scenario ===
export function customerFlow(data) {
  const users = data.users;
  // Pick a random user from the pool
  const user = users[Math.floor(Math.random() * users.length)];

  const token = user.token;
  const userId = user.userId;

  try {
    const chance = Math.random();

    // 1. Browse courts (always)
    const resCanchas = getCanchasDisponibles(token);
    check(resCanchas, { "canchas status 200": (r) => r.status === 200 });
    
    // 2. Browse turns (always)
    const resTurnos = getTurnosDisponiblesDeCancha(token);
    check(resTurnos, { "turnos status 200": (r) => r.status === 200 });
    
    sleep(Math.random() * 0.5 + 0.5); // Think time

    // 3. Create Reservation (High probability: 60%)
    if (chance < 0.6) {
        const startCrear = Date.now();
        const okReserva = crearReserva(token);
        tCrear.add(Date.now() - startCrear);
        if (!okReserva) failsCrear.add(1);
    }

    // 4. View My Reservations (Always, maybe checking if the new one is there)
    const resReservas = getReservasDeUsuario(token, userId);
    check(resReservas, { "reservas status 200": (r) => r.status === 200 });

    // 5. Cancel Reservation (Low probability: 10%)
    if (chance > 0.9) {
        const startCancel = Date.now();
        const okCancel = cancelarReserva(token, userId);
        tCancelar.add(Date.now() - startCancel);
        if (!okCancel) failsCancelar.add(1);
    }

    // 6. New User Signup (Rare: 5%)
    // Simulating a new user coming to the platform
    if (Math.random() < 0.05) {
      const name = randomName();
      const email = randomEmail();
      const password = randomPassword();
      
      const start = Date.now();
      const res = signup(name, email, password);
      tSignup.add(Date.now() - start);
      if (!res || res.status !== 201) {
        failsSignup.add(1);
      }
      sleep(Math.random() * 0.5 + 0.5);
    }

    sleep(Math.random() * 1 + 0.5); // Pacing

  } catch (err) {
    console.error('❌ Error in customerFlow:', err);
  }
}
