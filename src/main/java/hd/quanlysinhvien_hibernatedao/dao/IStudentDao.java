
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IStudentDao extends IPaginationSortingDao<Long, Student> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDepartmentId(Long departmentId);

    // Tim sinh vien theo email (khong phan biet hoa thuong), kem khoa
    Optional<Student> findByEmailIgnoreCase(String email);

    Page<Student> search(
            String keyword,
            Long departmentId,
            Pageable pageable
    );
}
