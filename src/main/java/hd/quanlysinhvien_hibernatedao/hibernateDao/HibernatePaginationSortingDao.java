
package hd.quanlysinhvien_hibernatedao.hibernateDao;

import hd.quanlysinhvien_hibernatedao.dao.IPaginationSortingDao;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;

public class HibernatePaginationSortingDao<Pk, Entity>
        extends HibernateGenericDao<Pk, Entity>
        implements IPaginationSortingDao<Pk, Entity> {

    public HibernatePaginationSortingDao(
            Class<Entity> type,
            EntityManager entityManager
    ) {
        super(type, entityManager);
    }

    // 1. Lay tat ca du lieu co sap xep
    @Override
    public List<Entity> findAllSorted(
            String sortBy,
            String direction
    ) {
        Sort sort = buildSort(sortBy, direction);

        return findAll(sort);
    }

    // 2. Lay tat ca du lieu co phan trang va sap xep
    @Override
    public Page<Entity> findAllPaged(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = buildSort(sortBy, direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                sort
        );

        return findAll(pageable);
    }

    // 3. Tim theo thuoc tinh va sap xep bang Sort
    @Override
    public List<Entity> findByFieldSorted(
            String fieldName,
            Object value,
            Sort sort
    ) {
        if (!isValidIdentifier(fieldName)) {
            return Collections.emptyList();
        }

        Specification<Entity> specification =
                createFieldSpecification(
                        fieldName,
                        value
                );

        Sort safeSort = sort == null
                ? Sort.unsorted()
                : sort;

        return findAll(
                specification,
                safeSort
        );
    }

    // 4. Tim theo thuoc tinh voi sortBy va direction
    @Override
    public List<Entity> findByFieldSorted(
            String fieldName,
            Object value,
            String sortBy,
            String direction
    ) {
        return findByFieldSorted(
                fieldName,
                value,
                buildSort(sortBy, direction)
        );
    }

    // 5. Tim theo thuoc tinh va phan trang bang Pageable
    @Override
    public Page<Entity> findByFieldPaged(
            String fieldName,
            Object value,
            Pageable pageable
    ) {
        if (!isValidIdentifier(fieldName)) {
            return new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
            );
        }

        Specification<Entity> specification =
                createFieldSpecification(
                        fieldName,
                        value
                );

        return findAll(
                specification,
                pageable
        );
    }

    // 6. Tim theo thuoc tinh va phan trang bang tham so
    @Override
    public Page<Entity> findByFieldPaged(
            String fieldName,
            Object value,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                buildSort(sortBy, direction)
        );

        return findByFieldPaged(
                fieldName,
                value,
                pageable
        );
    }

    // 7. Tao dieu kien tim kiem bang JPA Specification
    private Specification<Entity> createFieldSpecification(
            String fieldName,
            Object value
    ) {
        return (root, query, builder) -> {

            // JPA xu ly ten thuoc tinh cua Entity
            var path = root.get(fieldName);

            // Giu nguyen hanh vi truy van cu khi value = null
            if (value == null) {
                return builder.disjunction();
            }

            return builder.equal(
                    path,
                    value
            );
        };
    }

    // 8. Tim kiem bang Specification
    @Override
    public List<Entity> findAll(
            Specification<Entity> spec
    ) {
        return super.findAll(spec);
    }

    // 9. Tim kiem bang Specification va phan trang
    @Override
    public Page<Entity> findAll(
            Specification<Entity> spec,
            Pageable pageable
    ) {
        return super.findAll(spec, pageable);
    }

    // 10. Tim kiem bang Specification va sap xep
    @Override
    public List<Entity> findAll(
            Specification<Entity> spec,
            Sort sort
    ) {
        return super.findAll(spec, sort);
    }

    // 11. Lay tat ca du lieu bang Pageable
    @Override
    public Page<Entity> getAll(
            Pageable pageable
    ) {
        return findAll(pageable);
    }

    // 12. Lay tat ca du lieu bang Sort
    @Override
    public List<Entity> getAll(
            Sort sort
    ) {
        return findAll(sort);
    }

    // 13. Tao doi tuong Sort
    protected Sort buildSort(
            String sortBy,
            String direction
    ) {
        if (sortBy == null
                || sortBy.isBlank()
                || !isValidIdentifier(sortBy)) {

            return Sort.unsorted();
        }

        return "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
    }
}
