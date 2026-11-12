// Stop/start only the support container before invoking this script with down/up.
const assert = require('node:assert/strict');
const base = process.env.GATEWAY_URL || 'http://localhost:8080';
const mode = process.argv[2];
async function support() {
    return fetch(base + '/api/v1/support/messages', { method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId: 'circuit-check', message: 'Booking help' }),
        signal: AbortSignal.timeout(15000) });
}
async function main() {
    assert.ok(['down','up'].includes(mode), 'Usage: node scripts/verify-circuit.cjs down|up');
    if (mode === 'down') {
        for (let i = 0; i < 8; i++) {
            const response = await support();
            assert.equal(response.status, 503);
            assert.equal((await response.json()).service, 'support');
        }
        const metric = await fetch(base + '/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:support&tag=state:open');
        const value = await metric.json();
        assert.ok(value.measurements.some(item => item.value === 1), 'Support breaker should be OPEN');
        assert.equal((await fetch(base + '/api/v1/tours')).status, 200);
        console.log('PASS support returns 503, circuit is OPEN, catalog remains available');
    } else {
        for (let i = 0; i < 60; i++) {
            if ((await support()).ok) {
                // A second successful probe closes the half-open breaker.
                if ((await support()).ok) {
                    console.log('PASS support recovered after restart');
                    return;
                }
            }
            await new Promise(resolve => setTimeout(resolve, 2000));
        }
        throw new Error('Support did not recover within 120 seconds');
    }
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
