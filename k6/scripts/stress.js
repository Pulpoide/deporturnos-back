/**
 * ============================================================
 * Stress Test - Deporturnos API
 * ============================================================
 *
 *  Objective:
 *   Evaluate system resilience and responsiveness under prolonged high load.
 *
 *  Simulated Flow (customerFlow):
 *   1. Existing users login (setup).
 *   2. Browse courts and turns.
 *   3. Create reservation (60% chance).
 *   4. View reservations.
 *   5. Cancel reservation (10% chance).
 *   6. New user signup (5% chance).
 *
 *  Configuration:
 *   - Executor: ramping-vus
 *   - Stages: Ramp up -> Sustain -> Ramp down
 *
 *  Author: Joaquin Olivero & Jules
 * ============================================================
 */

import { ensureAuth } from '../helpers/actions.js';
import { USERS } from '../helpers/data.js';
import { customerFlow } from '../scenarios/customer_flow.js';

// Re-export metrics from scenario if needed for thresholds
export { failsLogin, failsSignup, failsCrear, failsCancelar } from '../scenarios/customer_flow.js';

export function setup() {
  const loggedUsers = USERS.map(user => {
    const res = ensureAuth(user.email, user.password);
    if (res && res.token) {
      return {
        ...user,
        token: res.token,
        userId: res.userId,
      };
    } else {
      console.warn(`⚠️ Login and Signup failed for ${user.email}`);
      return null;
    }
  }).filter(u => u !== null);

  console.log(`✅ Setup complete: ${loggedUsers.length} users logged in.`);
  return { users: loggedUsers };
}

export default function(data) {
    if (!data.users || data.users.length === 0) {
        // Fallback or exit if no users
        return;
    }
    customerFlow(data);
}

export const options = {
  scenarios: {
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 10 }, // Ramp up to 10 users
        { duration: '1m', target: 20 },  // Ramp up to 20 users
        { duration: '2m', target: 20 },  // Stay at 20 users (soak)
        { duration: '30s', target: 0 },  // Ramp down
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'], // Error rate < 5%
    http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 2s
    login_failed: ['count<5'],
    signup_failed: ['count<10'],
    crear_reserva_failed: ['count<20'],
    cancelar_reserva_failed: ['count<20'],
  },
};
