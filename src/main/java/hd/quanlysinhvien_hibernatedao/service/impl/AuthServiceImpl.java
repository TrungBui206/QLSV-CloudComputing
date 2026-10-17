
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.enums.Role;
import hd.quanlysinhvien_hibernatedao.service.AuthService;
import hd.quanlysinhvien_hibernatedao.service.JwtTokenService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final IUserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthServiceImpl(
            IUserDao userDao,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        String username = request.username()
                .trim()
                .toLowerCase(Locale.ROOT);

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String password = request.password();

        if (username.equals("admin")
                || username.equals("lecturer")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên đăng nhập này được dành riêng"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mật khẩu quá dài đối với BCrypt"
            );
        }

        if (userDao.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên đăng nhập đã tồn tại"
            );
        }

        if (userDao.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email đã được sử dụng"
            );
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        try {
            User savedUser = userDao.create(user);
            userDao.flush();

            return UserResponse.from(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên đăng nhập hoặc email đã tồn tại",
                    e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String username = request.username()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userDao.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Tài khoản hoặc mật khẩu không đúng"
                        )
                );

        if (!user.isEnabled()
                || !passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Tài khoản hoặc mật khẩu không đúng"
            );
        }

        return jwtTokenService.generateToken(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {

        User user = userDao.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Tài khoản không tồn tại"
                        )
                );

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tài khoản đã bị vô hiệu hóa"
            );
        }

        return UserResponse.from(user);
    }
}
