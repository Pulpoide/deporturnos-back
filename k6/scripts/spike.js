/**
 * ============================================================
 * Spike Test - Deporturnos API
 * ============================================================
 *
 *  Objective:
 *   Evaluate system behavior under sudden bursts of traffic.
 *   Verify if the system recovers after the spike.
 *
 *  Configuration:
 *   - Executor: ramping-vus
 *   - Stages: Fast ramp up -> Short peak -> Fast ramp down
 *
 *  Author: Joaquin Olivero & Jules
 * ============================================================
 */

import { ensureAuth } from '../helpers/actions.js';
import { USERS } from '../helpers/data.js';
import { customerFlow } from '../scenarios/customer_flow.js';

// Re-export metrics
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
        return;
    }
    customerFlow(data);
}

export const options = {
  scenarios: {
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 5 },   // Warm up
        { duration: '20s', target: 50 },  // Spike! (Rapid increase)
        { duration: '1m', target: 50 },   // Sustain spike
        { duration: '10s', target: 0 },   // Recovery
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.10'], // Allow higher error rate during spike (10%)
    http_req_duration: ['p(95)<5000'], // Allow slower response times (5s)
    login_failed: ['count<10'],
  },
};
