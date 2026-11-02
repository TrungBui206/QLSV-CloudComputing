
package hd.quanlysinhvien_hibernatedao.security.scope;

import java.util.Optional;

/**
 * Phạm vi dữ liệu sinh viên mà người dùng hiện tại được phép xem.
 *
 * <ul>
 *     <li>STUDENT: chỉ được xem hồ sơ sinh viên có email trùng với email
 *     tài khoản (không phân biệt hoa thường) - trả về email đó.</li>
 *     <li>ADMIN, LECTURER, hoặc lời gọi nội bộ không có người dùng đăng nhập:
 *     trả về {@link Optional#empty()} - không giới hạn.</li>
 * </ul>
 *
 * <p>Lưu ý bảo mật: liên kết theo email chỉ an toàn khi email tài khoản
 * đáng tin cậy. Khi đăng ký công khai và chưa xác minh email, người đăng ký
 * bằng email của sinh viên khác sẽ xem được hồ sơ của sinh viên đó.</p>
 *
 * <p>Tầng web (SecurityConfig + @PreAuthorize) vẫn bắt buộc đăng nhập
 * cho mọi API /api/students.</p>
 */
public interface StudentDataScope {

    Optional<String> restrictedEmail();
}
