
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IClassInfoDao;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernateGenericDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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

        // Tao CriteriaBuilder
        CriteriaBuilder builder =
                entityManager.getCriteriaBuilder();

        // Tao truy van ClassInfo
        CriteriaQuery<ClassInfo> criteriaQuery =
                builder.createQuery(ClassInfo.class);

        Root<ClassInfo> root =
                criteriaQuery.from(ClassInfo.class);

        // Tham so tim kiem
        ParameterExpression<String> keywordParameter =
                builder.parameter(String.class, "keyword");

        // Tim theo ten lop
        Predicate namePredicate = builder.like(
                builder.lower(root.get("name")),
                keywordParameter
        );

        // Tim theo domain
        Predicate domainPredicate = builder.like(
                builder.lower(root.get("domain")),
                keywordParameter
        );

        // Xu ly description co the null
        Expression<String> descriptionExpression =
                builder.coalesce(
                        root.<String>get("description"),
                        ""
                );

        Predicate descriptionPredicate = builder.like(
                builder.lower(descriptionExpression),
                keywordParameter
        );

        // Ghep cac dieu kien tim kiem bang OR
        Predicate searchPredicate = builder.or(
                namePredicate,
                domainPredicate,
                descriptionPredicate
        );

        criteriaQuery.select(root);
        criteriaQuery.where(searchPredicate);

        // Chi cho phep sap xep theo cac truong hop le
        String sortProperty = switch (
                orderBy == null ? "" : orderBy
                ) {
            case "id" -> "id";
            case "domain" -> "domain";
            case "description" -> "description";
            default -> "name";
        };

        Expression<?> sortExpression =
                root.get(sortProperty);

        // Chieu sap xep ASC hoac DESC
        boolean descending =
                "desc".equalsIgnoreCase(orderType);

        Order primaryOrder = descending
                ? builder.desc(sortExpression)
                : builder.asc(sortExpression);

        // Sap xep theo id de on dinh thu tu
        Order secondaryOrder =
                builder.asc(root.get("id"));

        criteriaQuery.orderBy(
                primaryOrder,
                secondaryOrder
        );

        // Thuc thi truy van
        TypedQuery<ClassInfo> typedQuery =
                entityManager.createQuery(criteriaQuery);

        typedQuery.setParameter(
                "keyword",
                "%" + keyword + "%"
        );

        typedQuery.setFirstResult(lowerLimit);
        typedQuery.setMaxResults(
                upperLimit - lowerLimit
        );

        return typedQuery.getResultList();
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
