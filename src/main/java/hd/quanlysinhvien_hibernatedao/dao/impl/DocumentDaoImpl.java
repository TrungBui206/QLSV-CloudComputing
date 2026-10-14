
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernateGenericDao;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<Document> findActiveByAppId(String appId) {
        return entityManager.createQuery(
                        """
                        SELECT d FROM Document d
                        WHERE d.appId = :appId
                          AND d.deleted = false
                        ORDER BY d.creationTime DESC
                        """,
                        Document.class
                )
                .setParameter("appId", appId)
                .getResultList();
    }
}
