package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.rest.AuthRest;
import hd.quanlysinhvien_hibernatedao.service.AuthService;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(
        name = "Xác thực và tài khoản",
        description = "Các API đăng ký, đăng nhập và xác thực người dùng"
)
@RestController
@RequestMapping("/api/auth")
public class AuthRestImpl implements AuthRest {

    private final AuthService authService;

    public AuthRestImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> register(
            RegisterRequest request
    ) {
        UserResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Đăng ký tài khoản thành công",
                                response
                        )
                );
    }

    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đăng nhập thành công",
                        response
                )
        );
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Người dùng chưa đăng nhập"
            );
        }

        String username = authentication.getName();

        UserResponse response =
                authService.getCurrentUser(username);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin người dùng thành công",
                        response
                )
        );
    }
}