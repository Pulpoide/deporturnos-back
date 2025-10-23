import { sleep } from 'k6';
import {login, getCanchasDisponibles, getTurnosDisponiblesDeCancha} from "./api/actions.js";

export let options = {
    stages: [
        { duration: '10m', target: 1000 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    const token = login("admin@supreme.com", "password123/*");
    getCanchasDisponibles(token);
    getTurnosDisponiblesDeCancha(token);

    sleep(1);
}



