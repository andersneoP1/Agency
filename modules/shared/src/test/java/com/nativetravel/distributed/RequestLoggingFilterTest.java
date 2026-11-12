package com.nativetravel.distributed;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {
    @Test
    void propagatesCorrelationAndClearsThreadContext() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/tours");
        request.addHeader("X-Request-Id", "trace-123");
        var response = new MockHttpServletResponse();
        new RequestLoggingFilter().doFilter(request, response,
                (req, res) -> assertThat(MDC.get("requestId")).isEqualTo("trace-123"));
        assertThat(response.getHeader("X-Request-Id")).isEqualTo("trace-123");
        assertThat(MDC.get("requestId")).isNull();
    }
}
