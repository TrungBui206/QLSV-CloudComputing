
package hd.quanlysinhvien_hibernatedao.security.scope.impl;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;
import hd.quanlysinhvien_hibernatedao.security.scope.StudentDataScope;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;

@Component
public class StudentDataScopeImpl implements StudentDataScope {

    private static final String STUDENT_AUTHORITY =
            "ROLE_" + Role.STUDENT.name();

    private final IUserDao userDao;

    public StudentDataScopeImpl(IUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<String> restrictedEmail() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // Khong co nguoi dung dang nhap hoac khong phai STUDENT
        // => khong gioi han pham vi
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !isStudent(authentication)) {
            return Optional.empty();
        }

        // JWT subject = username (xem JwtTokenServiceImpl)
        User user = userDao.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Tài khoản không tồn tại"
                        )
                );

        return Optional.of(
                user.getEmail()
                        .trim()
                        .toLowerCase(Locale.ROOT)
        );
    }

    private boolean isStudent(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        STUDENT_AUTHORITY.equals(
                                authority.getAuthority()
                        )
                );
    }
}
