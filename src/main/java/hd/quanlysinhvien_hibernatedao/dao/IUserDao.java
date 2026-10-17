
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.User;

import java.util.Optional;

public interface IUserDao
        extends IGenericDao<String, User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
