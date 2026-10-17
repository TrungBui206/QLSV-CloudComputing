
package hd.quanlysinhvien_hibernatedao.config;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.enums.Role;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(
        name = "app.bootstrap.enabled",
        havingValue = "true"
)
public class BootstrapUsersConfig {

    @Bean
    public CommandLineRunner bootstrapUsers(
            IUserDao userDao,
            PasswordEncoder passwordEncoder,
            @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}")
            String adminPassword,
            @Value("${APP_BOOTSTRAP_LECTURER_PASSWORD:}")
            String lecturerPassword
    ) {
        return args -> {

            validatePassword(adminPassword);
            validatePassword(lecturerPassword);

            createIfMissing(
                    userDao,
                    passwordEncoder,
                    "admin",
                    "admin@local.invalid",
                    adminPassword,
                    Role.ADMIN
            );

            createIfMissing(
                    userDao,
                    passwordEncoder,
                    "lecturer",
                    "lecturer@local.invalid",
                    lecturerPassword,
                    Role.LECTURER
            );
        };
    }

    private void validatePassword(String password) {

        int byteLength = password
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (password.length() < 12 || byteLength > 72) {
            throw new IllegalStateException(
                    "Mật khẩu khởi tạo phải có ít nhất 12 ký tự và không quá 72 byte"
            );
        }
    }

    private void createIfMissing(
            IUserDao userDao,
            PasswordEncoder passwordEncoder,
            String username,
            String email,
            String password,
            Role role
    ) {

        if (userDao.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(password)
        );
        user.setRole(role);
        user.setEnabled(true);

        userDao.create(user);
    }
}
