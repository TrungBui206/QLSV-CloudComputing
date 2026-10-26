package hd.quanlysinhvien_hibernatedao.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
public class SecurityExceptionHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver exceptionResolver;

    private final AuthenticationEntryPoint bearerEntryPoint =
            new BearerTokenAuthenticationEntryPoint();

    private final AccessDeniedHandler bearerAccessDeniedHandler =
            new BearerTokenAccessDeniedHandler();

    public SecurityExceptionHandler(
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver
    ) {
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        bearerEntryPoint.commence(
                request,
                response,
                authException
        );

        exceptionResolver.resolveException(
                request,
                response,
                null,
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Bạn chưa đăng nhập hoặc phiên đăng nhập không hợp lệ"
                )
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        bearerAccessDeniedHandler.handle(
                request,
                response,
                accessDeniedException
        );

        exceptionResolver.resolveException(
                request,
                response,
                null,
                new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Bạn không có quyền thực hiện thao tác này"
                )
        );
    }
}