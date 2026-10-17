
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IClassInfoDao;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernateGenericDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class ClassInfoDaoImpl
        extends HibernateGenericDao<String, ClassInfo>
        implements IClassInfoDao {

    private final EntityManager entityManager;

    public ClassInfoDaoImpl(EntityManager entityManager) {
        super(ClassInfo.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ClassInfo> findByNameIgnoreCase(String name) {
        return entityManager.createQuery(
                        """
                        SELECT c FROM ClassInfo c
                        WHERE LOWER(c.name) = LOWER(:name)
                        """,
                        ClassInfo.class
                )
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<ClassInfo> findByDomainIgnoreCase(String domain) {
        return entityManager.createQuery(
                        """
                        SELECT c FROM ClassInfo c
                        WHERE LOWER(c.domain) = LOWER(:domain)
                        ORDER BY c.name ASC
                        """,
                        ClassInfo.class
                )
                .setParameter("domain", domain)
                .getResultList();
    }

    @Override
    public List<ClassInfo> search(
            String query,
            int lowerLimit,
            int upperLimit,
            String orderBy,
            String orderType
    ) {

        String keyword = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        String sortField = switch (
                orderBy == null ? "" : orderBy
                ) {
            case "id" -> "c.id";
            case "domain" -> "c.domain";
            case "description" -> "c.description";
            default -> "c.name";
        };

        String direction =
                "desc".equalsIgnoreCase(orderType)
                        ? "DESC"
                        : "ASC";

        String jpql = """
                SELECT c FROM ClassInfo c
                WHERE (
                    LOWER(c.name) LIKE :keyword
                    OR LOWER(c.domain) LIKE :keyword
                    OR LOWER(COALESCE(c.description, '')) LIKE :keyword
                )
                ORDER BY
                """ + sortField + " " + direction + ", c.id ASC";

        TypedQuery<ClassInfo> typedQuery =
                entityManager.createQuery(jpql, ClassInfo.class);

        return typedQuery
                .setParameter("keyword", "%" + keyword + "%")
                .setFirstResult(lowerLimit)
                .setMaxResults(upperLimit - lowerLimit)
                .getResultList();
    }

    @Override
    public long countSearch(String query) {
        String keyword = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        return entityManager.createQuery(
                        """
                        SELECT COUNT(c) FROM ClassInfo c
                        WHERE (
                            LOWER(c.name) LIKE :keyword
                            OR LOWER(c.domain) LIKE :keyword
                            OR LOWER(COALESCE(c.description, '')) LIKE :keyword
                        )
                        """,
                        Long.class
                )
                .setParameter("keyword", "%" + keyword + "%")
                .getSingleResult();
    }
}
