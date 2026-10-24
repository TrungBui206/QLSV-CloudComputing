
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.common.util.SearchUtils;
import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernatePaginationSortingDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
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
import java.util.Set;

@Repository
public class StudentDaoImpl
        extends HibernatePaginationSortingDao<Long, Student>
        implements IStudentDao {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "name", "email", "dob", "createdAt");

    public StudentDaoImpl(EntityManager entityManager) {
        super(Student.class, entityManager);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {

        String jpql =
                "SELECT COUNT(s) FROM Student s WHERE s.email = :email";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("email", email)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAndIdNot(String email, Long id) {

        String jpql =
                "SELECT COUNT(s) FROM Student s "
                        + "WHERE s.email = :email AND s.id <> :id";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("email", email)
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByDepartmentId(Long departmentId) {

        String jpql =
                "SELECT COUNT(s) FROM Student s "
                        + "WHERE s.department.id = :departmentId";

        Long count = getEntityManager()
                .createQuery(jpql, Long.class)
                .setParameter("departmentId", departmentId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Student> search(
            String keyword,
            Long departmentId,
            Pageable pageable
    ) {

        // 1. Kiem tra truong sap xep
        validateSort(pageable.getSort());

        // 2. Chuan hoa tu khoa bang SearchUtils
        String normalizedKeyword =
                SearchUtils.normalize(keyword);

        String pattern = "%" + normalizedKeyword + "%";

        CriteriaBuilder builder =
                getEntityManager().getCriteriaBuilder();

        // 3. Tao truy van dem sinh vien
        CriteriaQuery<Long> countQuery =
                builder.createQuery(Long.class);

        Root<Student> countRoot =
                countQuery.from(Student.class);

        countQuery.select(
                builder.count(countRoot)
        );

        countQuery.where(
                buildSearchPredicate(
                        builder,
                        countRoot,
                        normalizedKeyword,
                        departmentId
                )
        );

        TypedQuery<Long> countTypedQuery =
                getEntityManager().createQuery(countQuery);

        if (!normalizedKeyword.isEmpty()) {
            countTypedQuery.setParameter("pattern", pattern);
        }

        Long total = countTypedQuery.getSingleResult();

        // 4. Tao truy van lay danh sach sinh vien
        CriteriaQuery<Student> dataQuery =
                builder.createQuery(Student.class);

        Root<Student> root =
                dataQuery.from(Student.class);

        // Giu lai JOIN FETCH department
        root.fetch("department", JoinType.INNER);

        dataQuery.select(root);

        dataQuery.where(
                buildSearchPredicate(
                        builder,
                        root,
                        normalizedKeyword,
                        departmentId
                )
        );

        // 5. Sap xep
        List<Order> orders = buildSortOrders(
                builder,
                root,
                pageable.getSort()
        );

        dataQuery.orderBy(orders);

        // 6. Thuc thi truy van
        TypedQuery<Student> query =
                getEntityManager().createQuery(dataQuery);

        if (!normalizedKeyword.isEmpty()) {
            query.setParameter("pattern", pattern);
        }

        // 7. Phan trang
        query.setFirstResult(
                Math.toIntExact(pageable.getOffset())
        );

        query.setMaxResults(
                pageable.getPageSize()
        );

        List<Student> students =
                query.getResultList();

        return new PageImpl<>(
                students,
                pageable,
                total
        );
    }

    private Predicate buildSearchPredicate(
            CriteriaBuilder builder,
            Root<Student> root,
            String keyword,
            Long departmentId
    ) {

        List<Predicate> predicates = new ArrayList<>();

        // Tim kiem theo ten hoac email
        if (!keyword.isEmpty()) {

            ParameterExpression<String> patternParameter =
                    builder.parameter(String.class, "pattern");

            Predicate namePredicate = builder.like(
                    builder.lower(root.<String>get("name")),
                    patternParameter
            );

            Predicate emailPredicate = builder.like(
                    builder.lower(root.<String>get("email")),
                    patternParameter
            );

            predicates.add(
                    builder.or(
                            namePredicate,
                            emailPredicate
                    )
            );
        }

        // Loc theo khoa
        if (departmentId != null) {

            Predicate departmentPredicate = builder.equal(
                    root.get("department").get("id"),
                    departmentId
            );

            predicates.add(departmentPredicate);
        }

        // Khong co dieu kien thi lay tat ca
        if (predicates.isEmpty()) {
            return builder.conjunction();
        }

        return builder.and(
                predicates.toArray(new Predicate[0])
        );
    }

    private List<Order> buildSortOrders(
            CriteriaBuilder builder,
            Root<Student> root,
            Sort sort
    ) {

        List<Order> orders = new ArrayList<>();

        // Mac dinh sap xep theo id tang dan
        if (sort == null || sort.isUnsorted()) {

            orders.add(
                    builder.asc(root.get("id"))
            );

            return orders;
        }

        for (Sort.Order order : sort) {

            String property = order.getProperty();

            if (!SORT_FIELDS.contains(property)) {
                throw new IllegalArgumentException(
                        "Trường sắp xếp không hợp lệ: "
                                + property
                );
            }

            if (order.isAscending()) {
                orders.add(
                        builder.asc(root.get(property))
                );
            } else {
                orders.add(
                        builder.desc(root.get(property))
                );
            }
        }

        return orders;
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
}
