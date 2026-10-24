
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IAcademicYearDao;
import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernatePaginationSortingDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Repository
public class AcademicYearDaoImpl
        extends HibernatePaginationSortingDao<Long, AcademicYear>
        implements IAcademicYearDao {

    private static final Set<String> SORT_FIELDS =
            Set.of(
                    "id",
                    "code",
                    "name",
                    "startYear",
                    "endYear",
                    "createdAt"
            );

    public AcademicYearDaoImpl(EntityManager entityManager) {
        super(AcademicYear.class, entityManager);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {

        String jpql =
                "SELECT COUNT(e) FROM AcademicYear e WHERE e.code = :code";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("code", code)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {

        String jpql =
                "SELECT COUNT(e) FROM AcademicYear e "
                        + "WHERE e.code = :code AND e.id <> :id";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("code", code)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AcademicYear>
    findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name,
            String code,
            Pageable pageable
    ) {

        validateSort(pageable.getSort());

        String nameKeyword = "%" + normalize(name) + "%";
        String codeKeyword = "%" + normalize(code) + "%";

        CriteriaBuilder builder =
                getEntityManager().getCriteriaBuilder();

        // Truy vấn đếm tổng số bản ghi
        CriteriaQuery<Long> countQuery =
                builder.createQuery(Long.class);

        Root<AcademicYear> countRoot =
                countQuery.from(AcademicYear.class);

        countQuery.select(builder.count(countRoot));

        countQuery.where(
                buildSearchPredicate(builder, countRoot)
        );

        Long total = getEntityManager()
                .createQuery(countQuery)
                .setParameter("name", nameKeyword)
                .setParameter("code", codeKeyword)
                .getSingleResult();

        // Truy vấn lấy danh sách niên khóa
        CriteriaQuery<AcademicYear> dataQuery =
                builder.createQuery(AcademicYear.class);

        Root<AcademicYear> root =
                dataQuery.from(AcademicYear.class);

        dataQuery.select(root);

        dataQuery.where(
                buildSearchPredicate(builder, root)
        );

        // Sắp xếp theo các trường hợp lệ
        List<Order> orders = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {

            String property = sortOrder.getProperty();

            Expression<?> expression;

            if (sortOrder.isIgnoreCase()
                    && ("name".equals(property)
                    || "code".equals(property))) {

                expression = builder.lower(
                        root.get(property)
                );

            } else {

                expression = root.get(property);
            }

            if (sortOrder.isAscending()) {

                orders.add(builder.asc(expression));

            } else {

                orders.add(builder.desc(expression));
            }
        }

        if (!orders.isEmpty()) {
            dataQuery.orderBy(orders);
        }

        TypedQuery<AcademicYear> query =
                getEntityManager()
                        .createQuery(dataQuery);

        query.setParameter("name", nameKeyword);
        query.setParameter("code", codeKeyword);

        query.setFirstResult(
                Math.toIntExact(pageable.getOffset())
        );

        query.setMaxResults(
                pageable.getPageSize()
        );

        List<AcademicYear> academicYears =
                query.getResultList();

        return new PageImpl<>(
                academicYears,
                pageable,
                total
        );
    }

    private Predicate buildSearchPredicate(
            CriteriaBuilder builder,
            Root<AcademicYear> root
    ) {

        Predicate namePredicate = builder.like(
                builder.lower(root.get("name")),
                builder.parameter(String.class, "name")
        );

        Predicate codePredicate = builder.like(
                builder.lower(root.get("code")),
                builder.parameter(String.class, "code")
        );

        return builder.or(
                namePredicate,
                codePredicate
        );
    }

    private void validateSort(Sort sort) {

        for (Sort.Order order : sort) {

            if (!SORT_FIELDS.contains(order.getProperty())) {

                throw new IllegalArgumentException(
                        "Trường sắp xếp không hợp lệ: "
                                + order.getProperty()
                );
            }
        }
    }

    private String normalize(String value) {

        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
