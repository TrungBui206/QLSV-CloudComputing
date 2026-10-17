
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.rest.AuthRest;
import hd.quanlysinhvien_hibernatedao.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestImpl implements AuthRest {

    private final AuthService authService;

    public AuthRestImpl(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<UserResponse> register(
            @Valid RegisterRequest request
    ) {
        UserResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    public ResponseEntity<AuthResponse> login(
            @Valid LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponse> getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String username = authentication.getName();

        UserResponse response = authService.getCurrentUser(username);

        return ResponseEntity.ok(response);
    }
}
