
package hd.quanlysinhvien_hibernatedao.security.scope.impl;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.entity.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentDataScopeImplTest {

    @Mock
    private IUserDao userDao;

    private StudentDataScopeImpl scope;

    @BeforeEach
    void setUp() {
        scope = new StudentDataScopeImpl(userDao);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                )
        );
    }

    @Test
    void noAuthentication_isUnrestricted() {
        assertTrue(scope.restrictedEmail().isEmpty());
        verifyNoInteractions(userDao);
    }

    @Test
    void anonymous_isUnrestricted() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "key",
                        "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        assertTrue(scope.restrictedEmail().isEmpty());
        verifyNoInteractions(userDao);
    }

    @Test
    void notAuthenticatedToken_isUnrestricted() {
        // Constructor 2 tham so => isAuthenticated() = false
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student01", "n/a")
        );

        assertTrue(scope.restrictedEmail().isEmpty());
        verifyNoInteractions(userDao);
    }

    @Test
    void admin_isUnrestricted() {
        loginAs("admin", "ADMIN");

        assertTrue(scope.restrictedEmail().isEmpty());
        verifyNoInteractions(userDao);
    }

    @Test
    void lecturer_isUnrestricted() {
        loginAs("lecturer", "LECTURER");

        assertTrue(scope.restrictedEmail().isEmpty());
        verifyNoInteractions(userDao);
    }

    @Test
    void student_isRestrictedToOwnEmail_normalized() {
        loginAs("student01", "STUDENT");

        User user = new User();
        user.setUsername("student01");
        user.setEmail("  Student01@Test.Local ");

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        assertEquals(
                Optional.of("student01@test.local"),
                scope.restrictedEmail()
        );
    }

    @Test
    void student_accountMissing_returns401() {
        loginAs("ghost", "STUDENT");

        when(userDao.findByUsername("ghost"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> scope.restrictedEmail()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
