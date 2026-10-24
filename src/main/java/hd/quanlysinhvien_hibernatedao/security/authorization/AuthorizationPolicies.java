
package hd.quanlysinhvien_hibernatedao.security.authorization;

public final class AuthorizationPolicies {

    private AuthorizationPolicies() {
        // Khong cho phep khoi tao doi tuong
    }

    // Chi ADMIN duoc truy cap
    public static final String ADMIN_ONLY =
            "hasRole('ADMIN')";

    // ADMIN hoac LECTURER duoc truy cap
    public static final String ADMIN_OR_LECTURER =
            "hasAnyRole('ADMIN', 'LECTURER')";

    // Ca 3 vai tro duoc truy cap
    public static final String ALL_ROLES =
            "hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')";
}
