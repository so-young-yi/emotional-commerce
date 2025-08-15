import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '1m',
};

export default function () {
    let res = http.get('http://localhost:8080/api/v1/products?page=0&size=20&sort=likes_desc');
    check(res, { 'status was 200': (r) => r.status == 200 });
    sleep(1);
}
