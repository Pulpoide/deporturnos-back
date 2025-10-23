import { sleep } from 'k6';
import {login, getTurnosDisponiblesDeCancha, getCanchasDisponibles} from "./api/actions.js";

export let options = {
    stages: [
        { duration: '30s', target: 1500 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<8000'],
    },
};

export default function () {
    const token = login("joacolivero@gmail.com", "password123/*");
    getCanchasDisponibles(token);
    getTurnosDisponiblesDeCancha(token);

    sleep(1);
}