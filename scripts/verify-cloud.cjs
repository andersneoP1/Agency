// Run against a disposable database: creates a customer, booking and simulated payment.
const assert = require('node:assert/strict');
const gateway = process.env.GATEWAY_URL || 'http://localhost:8080';
const discovery = process.env.EUREKA_URL || 'http://localhost:8761';
const config = process.env.CONFIG_SERVER_URL || 'http://localhost:8888';
const prometheus = process.env.PROMETHEUS_URL || 'http://localhost:9090';
const services = ['identity','catalog','booking','payments','marketing','communications','support'];
async function request(base, path, options = {}) {
    const result = await fetch(base + path, { ...options, signal: AbortSignal.timeout(20000) });
    assert.ok(result.ok, `${path}: expected success, received ${result.status}`);
    return result;
}
async function json(base, path, options) { return (await request(base, path, options)).json(); }
async function ready() {
    for (let i = 0; i < 90; i++) {
        try {
            const registry = await json(discovery, '/eureka/apps', { headers: { Accept: 'application/json' } });
            const apps = registry.applications.application || [];
            if (services.every(name => apps.some(app => app.name.toLowerCase() === name &&
                [].concat(app.instance).some(instance => instance.status === 'UP')))) {
                await Promise.all(services.map(name => request(gateway, `/openapi/${name}`)));
                return;
            }
        } catch (_) { /* Config import, registration and registry caches need time to converge. */ }
        await new Promise(resolve => setTimeout(resolve, 2000));
    }
    throw new Error('Seven services did not become discoverable within 180 seconds');
}
async function main() {
    await ready();
    const settings = await json(config, '/catalog/distributed');
    assert.equal(settings.name, 'catalog');
    assert.ok(settings.propertySources.some(source => source.source['spring.datasource.url']));
    const expected = {
        identity: '/api/v1/auth/login', catalog: '/api/v1/tours', booking: '/api/v1/bookings',
        payments: '/api/v1/payments', marketing: '/api/v1/promocodes/validate',
        communications: '/api/v1/documents/{id}/voucher', support: '/api/v1/support/messages'
    };
    for (const name of services) {
        const schema = await json(gateway, `/openapi/${name}`);
        assert.ok(schema.paths[expected[name]], `${name}: missing owned API`);
        assert.ok(schema.info.title.includes(name), `${name}: incorrect API title`);
        assert.equal(schema.servers[0].url, '/', `${name}: Swagger must use the caller's Gateway origin`);
        for (const other of services.filter(s => s !== name)) {
            assert.ok(!schema.paths[expected[other]], `${name}: unexpectedly contains ${other} API`);
        }
    }
    console.log('PASS Config Server, Eureka and seven isolated OpenAPI specifications');
    const email = `cloud-check-${Date.now()}@example.test`;
    const post = (path, body, headers = {}) => json(gateway, path, { method: 'POST',
        headers: { 'Content-Type': 'application/json', ...headers }, body: JSON.stringify(body) });
    const auth = await post('/api/v1/auth/register', {
        firstName: 'Cloud', lastName: 'Check', email, password: 'CloudTest123!'
    });
    assert.ok(auth.accessToken);
    const headers = { Authorization: `Bearer ${auth.accessToken}`, 'X-Request-Id': 'cloud-check-booking' };
    const tours = await json(gateway, '/api/v1/tours?size=100');
    const tour = tours.content.find(item => item.availableSlots > 0);
    assert.ok(tour, 'A seeded tour with available inventory is required');
    const promo = await post('/api/v1/promocodes/validate', { code: 'WELCOME10', orderAmount: 100000 }, headers);
    assert.equal(promo.discountPercent, 10);
    const booking = await post('/api/v1/bookings', {
        tourId: tour.id, promoCode: 'WELCOME10', paymentMethod: 'CARD'
    }, headers);
    assert.equal(booking.status, 'PENDING');
    const payment = { bookingId: booking.id, paymentMethod: 'CARD', cardNumber: '4276 3800 1234 5678',
        cardHolder: 'CLOUD CHECK', cardExpiry: '12/29', cardCvv: '123' };
    const paid = await post('/api/v1/payments', payment, headers);
    assert.equal(paid.status, 'PAID');
    assert.ok(paid.qrCodeBase64.startsWith('data:image/png;base64,'));
    const repeated = await post('/api/v1/payments', payment, headers);
    assert.equal(repeated.bookingId, paid.bookingId);
    const voucher = await request(gateway, `/api/v1/documents/${booking.id}/voucher`, { headers });
    const pdf = Buffer.from(await voucher.arrayBuffer());
    assert.equal(pdf.subarray(0, 4).toString(), '%PDF');
    assert.equal(voucher.headers.get('X-Request-Id'), 'cloud-check-booking');
    await json(gateway, '/api/v1/profile/referral', { headers });
    await post('/api/v1/support/messages', { sessionId: `check-${Date.now()}`, message: 'How can I book a tour?' });
    await request(gateway, '/ws/info');
    assert.equal((await fetch(gateway + '/internal/payments/process', { method: 'POST' })).status, 404);
    console.log('PASS registration, catalog, marketing RPC, payment, QR/PDF RPC, referral RPC, support and SockJS');
    // Prometheus may not have scraped a just-started service yet.
    for (let i = 0; i < 10; i++) {
        const result = await json(prometheus, '/api/v1/query?query=up%7Bjob%3D%22native-travel%22%7D');
        if (services.every(name => result.data.result.some(row => row.metric.service === name && row.value[1] === '1'))) {
            console.log('PASS Prometheus scrapes all seven services');
            return;
        }
        await new Promise(resolve => setTimeout(resolve, 3000));
    }
    throw new Error('Prometheus does not report all seven services as UP');
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
