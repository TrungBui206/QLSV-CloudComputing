
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernateGenericDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DocumentDaoImpl
        extends HibernateGenericDao<String, Document>
        implements IDocumentDao {

    private final EntityManager entityManager;

    public DocumentDaoImpl(EntityManager entityManager) {
        super(Document.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentName(
            String fileName,
            String appId) {

        return entityManager.createNamedQuery(
                        "getDocumentByName",
                        Document.class
                )
                .setParameter("name", fileName)
                .setParameter("appId", appId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getLatestVersion(
            String fileName,
            String appId) {

        return entityManager.createQuery(
                        """
                        SELECT MAX(d.version)
                        FROM Document d
                        WHERE d.name = :name
                        AND d.appId = :appId
                        """,
                        Double.class
                )
                .setParameter("name", fileName)
                .setParameter("appId", appId)
                .getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getDocumentByAppId(String appId) {

        return entityManager.createNamedQuery(
                        "getDocumentByAppId",
                        Document.class
                )
                .setParameter("appId", appId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> findAllNotDeleted() {

        return entityManager.createQuery(
                """
                SELECT d FROM Document d
                WHERE d.deleted = false
                ORDER BY d.creationTime DESC
                """,
                Document.class
        ).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> findActiveById(String id) {

        return entityManager.createQuery(
                        """
                        SELECT d FROM Document d
                        WHERE d.id = :id
                        AND d.deleted = false
                        """,
                        Document.class
                )
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Document> findActiveByPath(String path) {

        return entityManager.createQuery(
                        """
                        SELECT d FROM Document d
                        WHERE d.path = :path
                        AND d.deleted = false
                        """,
                        Document.class
                )
                .setParameter("path", path)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> getDoc(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType,
            String appId) {

        StringBuilder jpql = new StringBuilder(
                """
                SELECT d FROM Document d
                WHERE d.deleted = false
                """
        );

        boolean filterApp =
                appId != null && !appId.isBlank();

        boolean filterKeyword =
                query != null && !query.isBlank();

        if (filterApp) {
            jpql.append(" AND d.appId = :appId");
        }

        if (filterKeyword) {
            jpql.append(
                    """
                     AND (
                        LOWER(d.name) LIKE :keyword
                        OR LOWER(d.appId) LIKE :keyword
                     )
                    """
            );
        }

        Map<String, String> sortFields = Map.of(
                "name", "name",
                "appId", "appId",
                "version", "version",
                "creationTime", "creationTime",
                "modificationTime", "modificationTime"
        );

        String sortField = sortFields.getOrDefault(
                orderBy,
                "creationTime"
        );

        String direction =
                "asc".equalsIgnoreCase(orderType)
                        ? "ASC"
                        : "DESC";

        jpql.append(
                " ORDER BY d." + sortField + " " + direction
        );

        TypedQuery<Document> typedQuery =
                entityManager.createQuery(
                        jpql.toString(),
                        Document.class
                );

        if (filterApp) {
            typedQuery.setParameter(
                    "appId",
                    appId.trim()
            );
        }

        if (filterKeyword) {
            typedQuery.setParameter(
                    "keyword",
                    "%" + query.trim().toLowerCase() + "%"
            );
        }

        int offset = lowerLimit == null
                ? 0
                : Math.max(0, lowerLimit);

        int limit = upperLimit == null
                ? 10
                : Math.max(1, Math.min(100, upperLimit));

        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

        return typedQuery.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDocCount(
            String query,
            String appId) {

        StringBuilder jpql = new StringBuilder(
                """
                SELECT COUNT(d) FROM Document d
                WHERE d.deleted = false
                """
        );

        boolean filterApp =
                appId != null && !appId.isBlank();

        boolean filterKeyword =
                query != null && !query.isBlank();

        if (filterApp) {
            jpql.append(" AND d.appId = :appId");
        }

        if (filterKeyword) {
            jpql.append(
                    """
                     AND (
                        LOWER(d.name) LIKE :keyword
                        OR LOWER(d.appId) LIKE :keyword
                     )
                    """
            );
        }

        TypedQuery<Long> typedQuery =
                entityManager.createQuery(
                        jpql.toString(),
                        Long.class
                );

        if (filterApp) {
            typedQuery.setParameter(
                    "appId",
                    appId.trim()
            );
        }

        if (filterKeyword) {
            typedQuery.setParameter(
                    "keyword",
                    "%" + query.trim().toLowerCase() + "%"
            );
        }

        return typedQuery.getSingleResult();
    }
}
