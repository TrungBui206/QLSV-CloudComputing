
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.entity.Department;
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
public class DepartmentDaoImpl
        extends HibernatePaginationSortingDao<Long, Department>
        implements IDepartmentDao {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "code", "name", "createdAt");

    public DepartmentDaoImpl(EntityManager entityManager) {
        super(Department.class, entityManager);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {

        String jpql =
                "SELECT COUNT(d) FROM Department d WHERE d.code = :code";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("code", code)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCodeAndIdNot(String code, Long id) {

        String jpql = "SELECT COUNT(d) FROM Department d "
                + "WHERE d.code = :code AND d.id <> :id";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("code", code)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Department>
    findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name,
            String code,
            Pageable pageable
    ) {

        // 1. Kiem tra cac truong sap xep
        validateSort(pageable.getSort());

        // 2. Chuan hoa du lieu tim kiem
        String nameKeyword = "%" + normalize(name) + "%";
        String codeKeyword = "%" + normalize(code) + "%";

        CriteriaBuilder builder =
                getEntityManager().getCriteriaBuilder();

        // 3. Tao truy van dem tong so khoa
        CriteriaQuery<Long> countQuery =
                builder.createQuery(Long.class);

        Root<Department> countRoot =
                countQuery.from(Department.class);

        countQuery.select(
                builder.count(countRoot)
        );

        countQuery.where(
                buildSearchPredicate(builder, countRoot)
        );

        Long total = getEntityManager()
                .createQuery(countQuery)
                .setParameter("name", nameKeyword)
                .setParameter("code", codeKeyword)
                .getSingleResult();

        // 4. Tao truy van lay danh sach khoa
        CriteriaQuery<Department> dataQuery =
                builder.createQuery(Department.class);

        Root<Department> root =
                dataQuery.from(Department.class);

        dataQuery.select(root);

        dataQuery.where(
                buildSearchPredicate(builder, root)
        );

        // 5. Xay dung dieu kien sap xep an toan
        List<Order> orders = new ArrayList<>();

        for (Sort.Order sortOrder : pageable.getSort()) {

            String property = sortOrder.getProperty();

            Expression<?> sortExpression;

            if (sortOrder.isIgnoreCase()
                    && ("name".equals(property)
                    || "code".equals(property))) {

                sortExpression = builder.lower(
                        root.<String>get(property)
                );

            } else {

                sortExpression = root.get(property);
            }

            if (sortOrder.isAscending()) {

                orders.add(
                        builder.asc(sortExpression)
                );

            } else {

                orders.add(
                        builder.desc(sortExpression)
                );
            }
        }

        if (!orders.isEmpty()) {
            dataQuery.orderBy(orders);
        }

        // 6. Thuc thi truy van
        TypedQuery<Department> query =
                getEntityManager().createQuery(dataQuery);

        query.setParameter("name", nameKeyword);
        query.setParameter("code", codeKeyword);

        // 7. Phan trang
        query.setFirstResult(
                Math.toIntExact(pageable.getOffset())
        );

        query.setMaxResults(
                pageable.getPageSize()
        );

        List<Department> departments =
                query.getResultList();

        // 8. Tra ve ket qua phan trang
        return new PageImpl<>(
                departments,
                pageable,
                total
        );
    }

    private Predicate buildSearchPredicate(
            CriteriaBuilder builder,
            Root<Department> root
    ) {

        Predicate namePredicate = builder.like(
                builder.lower(root.<String>get("name")),
                builder.parameter(String.class, "name")
        );

        Predicate codePredicate = builder.like(
                builder.lower(root.<String>get("code")),
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
