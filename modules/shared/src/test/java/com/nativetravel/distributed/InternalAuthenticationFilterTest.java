package com.nativetravel.distributed;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;

class InternalAuthenticationFilterTest {
    private final InternalAuthenticationFilter filter = new InternalAuthenticationFilter("test-service-token");

    @Test
    void rejectsInternalRequestsWithoutServiceCredentials() throws Exception {
        var request = new MockHttpServletRequest("POST", "/internal/payments/process");
        var response = new MockHttpServletResponse();
        var called = new AtomicBoolean();
        filter.doFilter(request, response, (req, res) -> called.set(true));
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(called).isFalse();
    }

    @Test
    void acceptsOnlyTheConfiguredToken() throws Exception {
        var request = new MockHttpServletRequest("POST", "/internal/email/reset");
        request.addHeader("X-Service-Token", "wrong-token");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> { throw new AssertionError("Must reject invalid token"); });
        assertThat(response.getStatus()).isEqualTo(403);

        request = new MockHttpServletRequest("POST", "/internal/email/reset");
        request.addHeader("X-Service-Token", "test-service-token");
        var accepted = new AtomicBoolean();
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> accepted.set(true));
        assertThat(accepted).isTrue();
    }

    @Test
    void leavesPublicEndpointsToSpringSecurity() throws Exception {
        var called = new AtomicBoolean();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/tours"),
                new MockHttpServletResponse(), (req, res) -> called.set(true));
        assertThat(called).isTrue();
    }
}
