
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AuthRest {

    @PostMapping("/register")
    ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    );

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    );

    @GetMapping("/me")
    ResponseEntity<UserResponse> getCurrentUser();
}
