
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IStudentDao extends IPaginationSortingDao<Long, Student> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDepartmentId(Long departmentId);

    Page<Student> search(
            String keyword,
            Long departmentId,
            Pageable pageable
    );
}
