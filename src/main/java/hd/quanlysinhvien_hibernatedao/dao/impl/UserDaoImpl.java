
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.hibernateDao.HibernateGenericDao;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl
        extends HibernateGenericDao<String, User>
        implements IUserDao {

    private final EntityManager entityManager;

    public UserDaoImpl(EntityManager entityManager) {
        super(User.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery(
                        """
                        SELECT u FROM User u
                        WHERE u.username = :username
                        """,
                        User.class
                )
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return entityManager.createQuery(
                        """
                        SELECT u FROM User u
                        WHERE u.email = :email
                        """,
                        User.class
                )
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
