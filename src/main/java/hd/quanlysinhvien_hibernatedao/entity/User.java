
package hd.quanlysinhvien_hibernatedao.entity;

import hd.quanlysinhvien_hibernatedao.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "username", nullable = false,
            unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false,
            unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false,
            length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false,
            length = 20)
    private Role role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
