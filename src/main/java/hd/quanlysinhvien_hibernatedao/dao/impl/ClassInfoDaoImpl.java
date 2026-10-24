
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.common.util.SearchUtils;
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

        // 1. Chuan hoa tu khoa bang SearchUtils
        String keyword = SearchUtils.normalize(query);

        // 2. Tao CriteriaBuilder
        CriteriaBuilder builder =
                entityManager.getCriteriaBuilder();

        // 3. Tao truy van ClassInfo
        CriteriaQuery<ClassInfo> criteriaQuery =
                builder.createQuery(ClassInfo.class);

        Root<ClassInfo> root =
                criteriaQuery.from(ClassInfo.class);

        // 4. Tham so tim kiem
        ParameterExpression<String> keywordParameter =
                builder.parameter(String.class, "keyword");

        // Tim theo ten lop
        Predicate namePredicate = builder.like(
                builder.lower(root.<String>get("name")),
                keywordParameter
        );

        // Tim theo domain
        Predicate domainPredicate = builder.like(
                builder.lower(root.<String>get("domain")),
                keywordParameter
        );

        // Tim theo description, xu ly gia tri null
        Expression<String> descriptionExpression =
                builder.coalesce(
                        root.<String>get("description"),
                        ""
                );

        Predicate descriptionPredicate = builder.like(
                builder.lower(descriptionExpression),
                keywordParameter
        );

        // 5. Ket hop cac dieu kien tim kiem
        Predicate searchPredicate = builder.or(
                namePredicate,
                domainPredicate,
                descriptionPredicate
        );

        criteriaQuery.select(root);
        criteriaQuery.where(searchPredicate);

        // 6. Chi cho phep sap xep theo cac truong hop le
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

        // 7. Sap xep ASC hoac DESC
        boolean descending =
                "desc".equalsIgnoreCase(orderType);

        Order primaryOrder = descending
                ? builder.desc(sortExpression)
                : builder.asc(sortExpression);

        // Sap xep phu theo id ASC
        Order secondaryOrder =
                builder.asc(root.get("id"));

        criteriaQuery.orderBy(
                primaryOrder,
                secondaryOrder
        );

        // 8. Thuc thi truy van
        TypedQuery<ClassInfo> typedQuery =
                entityManager.createQuery(criteriaQuery);

        typedQuery.setParameter(
                "keyword",
                "%" + keyword + "%"
        );

        // 9. Phan trang
        typedQuery.setFirstResult(lowerLimit);
        typedQuery.setMaxResults(
                upperLimit - lowerLimit
        );

        return typedQuery.getResultList();
    }

    @Override
    public long countSearch(String query) {

        // Dung chung SearchUtils
        String keyword = SearchUtils.normalize(query);

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
