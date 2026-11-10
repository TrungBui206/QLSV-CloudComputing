package hd.quanlysinhvien_hibernatedao.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Ghi một dòng log cho mỗi request: method, đường dẫn, mã trạng thái, thời gian xử lý.
 * Đặt trước Spring Security nên cả các request bị chặn (401/403) cũng được ghi.
 * Không ghi query string, header hay body để tránh lộ dữ liệu nhạy cảm.
 * Bỏ qua file tĩnh và Swagger để log gọn.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final List<String> SKIPPED_SUFFIXES = List.of(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".svg", ".ico", ".map", ".woff", ".woff2"
    );

    private static final List<String> SKIPPED_PREFIXES = List.of(
            "/swagger-ui", "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("{} {} -> {} ({} ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMs);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        String lower = path.toLowerCase();
        return SKIPPED_SUFFIXES.stream().anyMatch(lower::endsWith)
                || SKIPPED_PREFIXES.stream().anyMatch(lower::startsWith);
    }
}
