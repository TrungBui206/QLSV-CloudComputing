
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

import java.util.List;
import java.util.Optional;

public interface IClassInfoDao
        extends IGenericDao<String, ClassInfo> {

    Optional<ClassInfo> findByNameIgnoreCase(String name);

    List<ClassInfo> findByDomainIgnoreCase(String domain);

    List<ClassInfo> search(
            String query,
            int lowerLimit,
            int upperLimit,
            String orderBy,
            String orderType
    );

    long countSearch(String query);
}
