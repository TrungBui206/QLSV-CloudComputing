package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthRest {

    @Operation(
            summary = "Đăng ký tài khoản",
            description = "API công khai, không yêu cầu JWT. Tài khoản mới được đăng ký với vai trò STUDENT"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Đăng ký tài khoản thành công",
            useReturnTypeSchema = true
    )
    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    );

    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "API công khai, không yêu cầu JWT. Trả về Access Token sau khi xác thực thành công"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Đăng nhập thành công",
            useReturnTypeSchema = true
    )
    @PostMapping("/login")
    ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    );

    @Operation(
            summary = "Lấy thông tin tài khoản hiện tại",
            description = "Yêu cầu JWT hợp lệ của người dùng đã đăng nhập"
    )
    @GetMapping("/me")
    ResponseEntity<ApiResponse<UserResponse>> getCurrentUser();
}