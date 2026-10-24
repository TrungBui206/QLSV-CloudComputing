
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

    @Override
    public List<Entity> findAllSorted(
            String sortBy,
            String direction
    ) {
        Sort sort = buildSort(sortBy, direction);

        return findAll(sort);
    }

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

    // Xay dung dieu kien tim kiem bang Criteria API
    private Specification<Entity> createFieldSpecification(
            String fieldName,
            Object value
    ) {
        return (root, query, builder) -> {

            // JPA se kiem tra ten thuoc tinh cua entity.
            // Khong ghep fieldName vao chuoi JPQL.
            var path = root.get(fieldName);

            // Giu hanh vi cua dieu kien JPQL "= :value":
            // Neu value null thi khong co ban ghi khop.
            if (value == null) {
                return builder.disjunction();
            }

            return builder.equal(
                    path,
                    value
            );
        };
    }

    @Override
    public List<Entity> searchBySpecification(
            String search
    ) {
        throw new UnsupportedOperationException(
                "Specification search will be implemented later"
        );
    }

    @Override
    public List<Entity> searchBySpecificationSorted(
            String search,
            String sortBy,
            String direction
    ) {
        throw new UnsupportedOperationException(
                "Specification search will be implemented later"
        );
    }

    @Override
    public Page<Entity> searchBySpecificationPaged(
            String search,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        throw new UnsupportedOperationException(
                "Specification search will be implemented later"
        );
    }

    @Override
    public List<Entity> findAll(
            Specification<Entity> spec
    ) {
        return super.findAll(spec);
    }

    @Override
    public Page<Entity> findAll(
            Specification<Entity> spec,
            Pageable pageable
    ) {
        return super.findAll(spec, pageable);
    }

    @Override
    public List<Entity> findAll(
            Specification<Entity> spec,
            Sort sort
    ) {
        return super.findAll(spec, sort);
    }

    @Override
    public Page<Entity> getAll(
            Pageable pageable
    ) {
        return findAll(pageable);
    }

    @Override
    public List<Entity> getAll(
            Sort sort
    ) {
        return findAll(sort);
    }

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

    // Giu lai de tuong thich voi cac DAO ke thua
    // co the dang su dung phuong thuc nay.
    protected String buildOrderClause(
            Sort sort
    ) {
        if (sort == null || sort.isUnsorted()) {
            return "";
        }

        StringBuilder clause =
                new StringBuilder(" ORDER BY ");

        for (Sort.Order order : sort) {

            if (!isValidIdentifier(
                    order.getProperty()
            )) {
                throw new IllegalArgumentException(
                        "Invalid sort field"
                );
            }

            clause.append("e.")
                    .append(order.getProperty())
                    .append(" ")
                    .append(order.getDirection().name())
                    .append(", ");
        }

        return clause.substring(
                0,
                clause.length() - 2
        );
    }
}
