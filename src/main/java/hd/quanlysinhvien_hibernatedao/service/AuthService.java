
package hd.quanlysinhvien_hibernatedao.service;

import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(String username);
}
