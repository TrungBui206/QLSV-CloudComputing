
package hd.quanlysinhvien_hibernatedao.hibernateDao;

import hd.quanlysinhvien_hibernatedao.dao.IGenericDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HibernateGenericDao<Pk, Entity>
        extends SimpleJpaRepository<Entity, Pk>
        implements IGenericDao<Pk, Entity> {

    private final Class<Entity> type;
    private final EntityManager entityManager;

    public HibernateGenericDao(
            Class<Entity> type,
            EntityManager entityManager
    ) {
        super(type, entityManager);
        this.type = type;
        this.entityManager = entityManager;
    }

    // ==========================================
    // 1. THONG TIN ENTITY VA ENTITY MANAGER
    // ==========================================

    public Class<Entity> getType() {
        return type;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    // ==========================================
    // 2. CREATE - UPDATE
    // ==========================================

    @Override
    @Transactional
    public Entity create(Entity anEntity) {
        if (anEntity == null) {
            throw new IllegalArgumentException(
                    "Entity cannot be null"
            );
        }

        return super.save(anEntity);
    }

    @Override
    @Transactional
    public Entity update(Entity anEntity) {
        if (anEntity == null) {
            throw new IllegalArgumentException(
                    "Entity cannot be null"
            );
        }

        return super.saveAndFlush(anEntity);
    }

    @Override
    @Transactional
    public Entity saveOrUpdate(Entity anEntity) {
        if (anEntity == null) {
            throw new IllegalArgumentException(
                    "Entity cannot be null"
            );
        }

        return super.save(anEntity);
    }

    // ==========================================
    // 3. DELETE
    // ==========================================

    @Override
    @Transactional
    public void delete(Entity anEntity) {
        if (anEntity == null) {
            throw new IllegalArgumentException(
                    "Entity cannot be null"
            );
        }

        super.delete(anEntity);
    }

    @Override
    @Transactional
    public void deleteById(Pk id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ID cannot be null"
            );
        }

        super.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByPk(Pk id) {
        deleteById(id);
    }

    // ==========================================
    // 4. FIND BY ID
    // ==========================================

    @Override
    public Entity get(Pk id) {
        if (id == null) {
            return null;
        }

        return super.findById(id).orElse(null);
    }

    @Override
    public Optional<Entity> findById(Pk id) {
        if (id == null) {
            return Optional.empty();
        }

        return super.findById(id);
    }

    // ==========================================
    // 5. FIND BY FIELD - CRITERIA API
    // ==========================================

    @Override
    public Entity getByField(
            String fieldName,
            Object value
    ) {
        if (!isValidIdentifier(fieldName)) {
            return null;
        }

        List<Entity> results = createFieldQuery(
                fieldName,
                value
        )
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    @Override
    public List<Entity> findByField(
            String fieldName,
            Object value
    ) {
        if (!isValidIdentifier(fieldName)) {
            return Collections.emptyList();
        }

        return createFieldQuery(
                fieldName,
                value
        ).getResultList();
    }

    private TypedQuery<Entity> createFieldQuery(
            String fieldName,
            Object value
    ) {
        CriteriaBuilder builder =
                entityManager.getCriteriaBuilder();

        CriteriaQuery<Entity> criteriaQuery =
                builder.createQuery(type);

        Root<Entity> root =
                criteriaQuery.from(type);

        criteriaQuery.select(root);

        criteriaQuery.where(
                builder.equal(
                        root.get(fieldName),
                        value
                )
        );

        return entityManager.createQuery(criteriaQuery);
    }

    // ==========================================
    // 6. VALIDATE FIELD NAME
    // ==========================================

    protected boolean isValidIdentifier(
            String identifier
    ) {
        return identifier != null
                && identifier.matches(
                "^[a-zA-Z0-9_]+$"
        );
    }

    // ==========================================
    // 7. GET ALL
    // ==========================================

    @Override
    public List<Entity> getAll() {
        return super.findAll();
    }

    // ==========================================
    // 8. BULK OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public List<Entity> createAll(
            List<Entity> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return super.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteAll(
            List<Entity> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        super.deleteAll(entities);
    }

    // ==========================================
    // 9. EXISTS AND COUNT
    // ==========================================

    @Override
    public boolean exists(Pk id) {
        return id != null && super.existsById(id);
    }

    @Override
    public long count() {
        return super.count();
    }

    // ==========================================
    // 10. PERSISTENCE CONTEXT
    // ==========================================

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void clear() {
        entityManager.clear();
    }
}
