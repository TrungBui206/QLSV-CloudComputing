
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.entity.Department;
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
        String jpql = "SELECT COUNT(d) FROM Department d WHERE d.code = :code";

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
    public Page<Department> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Pageable pageable) {

        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Trường sắp xếp không hợp lệ: " + order.getProperty());
            }
        }

        String nameKeyword = "%" + normalize(name) + "%";
        String codeKeyword = "%" + normalize(code) + "%";

        String condition = " WHERE LOWER(e.name) LIKE :name "
                + "OR LOWER(e.code) LIKE :code";

        String countJpql = "SELECT COUNT(e) FROM Department e" + condition;

        Long total = getEntityManager()
                .createQuery(countJpql, Long.class)
                .setParameter("name", nameKeyword)
                .setParameter("code", codeKeyword)
                .getSingleResult();

        String jpql = "SELECT e FROM Department e"
                + condition
                + buildOrderClause(pageable.getSort());

        TypedQuery<Department> query = getEntityManager()
                .createQuery(jpql, Department.class);

        query.setParameter("name", nameKeyword);
        query.setParameter("code", codeKeyword);

        query.setFirstResult(Math.toIntExact(pageable.getOffset()));
        query.setMaxResults(pageable.getPageSize());

        List<Department> departments = query.getResultList();

        return new PageImpl<>(departments, pageable, total);
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
