
package hd.quanlysinhvien_hibernatedao.security.jwt;

import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.entity.User;

public interface JwtTokenService {

    AuthResponse generateToken(User user);
}
