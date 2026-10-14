
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernatePaginationSortingDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
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

        String jpql = "SELECT COUNT(s) FROM Student s "
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

        String jpql = "SELECT COUNT(s) FROM Student s "
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
            Pageable pageable) {

        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Trường sắp xếp không hợp lệ: "
                                + order.getProperty()
                );
            }
        }

        String kw = keyword == null
                ? ""
                : keyword.trim().toLowerCase(Locale.ROOT);

        String condition = """
                 WHERE (
                     :keyword = ''
                     OR LOWER(s.name) LIKE :pattern
                     OR LOWER(s.email) LIKE :pattern
                 )
                 AND (
                     :departmentId IS NULL
                     OR s.department.id = :departmentId
                 )
                """;

        String countJpql =
                "SELECT COUNT(s) FROM Student s " + condition;

        Long total = getEntityManager()
                .createQuery(countJpql, Long.class)
                .setParameter("keyword", kw)
                .setParameter("pattern", "%" + kw + "%")
                .setParameter("departmentId", departmentId)
                .getSingleResult();

        String jpql = "SELECT s FROM Student s "
                + "JOIN FETCH s.department "
                + condition
                + buildStudentOrderClause(pageable.getSort());

        TypedQuery<Student> query = getEntityManager()
                .createQuery(jpql, Student.class);

        query.setParameter("keyword", kw);
        query.setParameter("pattern", "%" + kw + "%");
        query.setParameter("departmentId", departmentId);

        query.setFirstResult(Math.toIntExact(pageable.getOffset()));
        query.setMaxResults(pageable.getPageSize());

        List<Student> students = query.getResultList();

        return new PageImpl<>(students, pageable, total);
    }

    private String buildStudentOrderClause(Sort sort) {

        if (sort == null || sort.isUnsorted()) {
            return " ORDER BY s.id ASC";
        }

        StringBuilder clause = new StringBuilder(" ORDER BY ");

        for (Sort.Order order : sort) {

            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Trường sắp xếp không hợp lệ: "
                                + order.getProperty()
                );
            }

            clause.append("s.")
                    .append(order.getProperty())
                    .append(" ")
                    .append(order.getDirection().name())
                    .append(", ");
        }

        clause.setLength(clause.length() - 2);

        return clause.toString();
    }
}
