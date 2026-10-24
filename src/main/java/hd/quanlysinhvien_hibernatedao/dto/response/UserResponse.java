
package hd.quanlysinhvien_hibernatedao.dto.response;

import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String username,
        String email,
        Role role,
        boolean enabled,
        LocalDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
