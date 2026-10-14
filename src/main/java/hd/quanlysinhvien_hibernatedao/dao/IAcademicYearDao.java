
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAcademicYearDao
        extends IPaginationSortingDao<Long, AcademicYear> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Page<AcademicYear> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name,
            String code,
            Pageable pageable
    );
}
