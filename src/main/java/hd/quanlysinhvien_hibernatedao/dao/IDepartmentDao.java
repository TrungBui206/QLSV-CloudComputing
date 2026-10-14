
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDepartmentDao extends IPaginationSortingDao<Long, Department> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Page<Department> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable);
}
