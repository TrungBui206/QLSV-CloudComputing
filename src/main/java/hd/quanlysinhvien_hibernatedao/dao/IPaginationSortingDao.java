
package hd.quanlysinhvien_hibernatedao.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface IPaginationSortingDao<Pk, Entity> extends IGenericDao<Pk, Entity> {

    List<Entity> findAllSorted(String sortBy, String direction);

    Page<Entity> findAllPaged(int page, int size, String sortBy, String direction);

    List<Entity> findByFieldSorted(String fieldName, Object value, Sort sort);

    List<Entity> findByFieldSorted(String fieldName, Object value, String sortBy, String direction);

    Page<Entity> findByFieldPaged(String fieldName, Object value, Pageable pageable);

    Page<Entity> findByFieldPaged(String fieldName, Object value, int page, int size, String sortBy, String direction);

    List<Entity> searchBySpecification(String search);

    List<Entity> searchBySpecificationSorted(String search, String sortBy, String direction);

    Page<Entity> searchBySpecificationPaged(String search, int page, int size, String sortBy, String direction);

    List<Entity> findAll(Specification<Entity> spec);

    Page<Entity> findAll(Specification<Entity> spec, Pageable pageable);

    List<Entity> findAll(Specification<Entity> spec, Sort sort);

    Page<Entity> getAll(Pageable pageable);

    List<Entity> getAll(Sort sort);
}
