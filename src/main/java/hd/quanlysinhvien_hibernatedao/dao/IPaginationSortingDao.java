
package hd.quanlysinhvien_hibernatedao.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface IPaginationSortingDao<Pk, Entity>
        extends IGenericDao<Pk, Entity> {

    // 1. Sap xep tat ca ban ghi
    List<Entity> findAllSorted(
            String sortBy,
            String direction
    );

    // 2. Phan trang va sap xep
    Page<Entity> findAllPaged(
            int page,
            int size,
            String sortBy,
            String direction
    );

    // 3. Tim kiem theo thuoc tinh va sap xep
    List<Entity> findByFieldSorted(
            String fieldName,
            Object value,
            Sort sort
    );

    List<Entity> findByFieldSorted(
            String fieldName,
            Object value,
            String sortBy,
            String direction
    );

    // 4. Tim kiem theo thuoc tinh va phan trang
    Page<Entity> findByFieldPaged(
            String fieldName,
            Object value,
            Pageable pageable
    );

    Page<Entity> findByFieldPaged(
            String fieldName,
            Object value,
            int page,
            int size,
            String sortBy,
            String direction
    );

    // 5. Tim kiem bang JPA Specification
    List<Entity> findAll(
            Specification<Entity> spec
    );

    Page<Entity> findAll(
            Specification<Entity> spec,
            Pageable pageable
    );

    List<Entity> findAll(
            Specification<Entity> spec,
            Sort sort
    );

    // 6. Lay tat ca du lieu
    Page<Entity> getAll(
            Pageable pageable
    );

    List<Entity> getAll(
            Sort sort
    );
}
