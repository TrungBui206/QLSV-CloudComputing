package hd.quanlysinhvien_hibernatedao.common.filter;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void logsMethodPathAndStatus(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/students");
        request.setQueryString("keyword=secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                ((HttpServletResponse) res).setStatus(404);
            }
        };

        filter.doFilter(request, response, chain);

        assertThat(output).contains("GET /api/students -> 404");
        assertThat(output).doesNotContain("keyword=secret");
    }

    @Test
    void logsEvenWhenChainThrows(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/departments");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                throw new IllegalStateException("boom");
            }
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(IllegalStateException.class);
        assertThat(output).contains("POST /api/departments ->");
    }

    @Test
    void skipsStaticFilesAndSwagger() {
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/menu.js"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/role-ui.CSS"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/images/logo.png"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/swagger-ui/index.html"))).isTrue();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/v3/api-docs"))).isTrue();
    }

    @Test
    void filtersApiPagesAndHealth() {
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/students"))).isFalse();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/login.html"))).isFalse();
        assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/health"))).isFalse();
        MockHttpServletRequest noUri = new MockHttpServletRequest();
        noUri.setRequestURI(null);
        assertThat(filter.shouldNotFilter(noUri)).isFalse();
    }
}
