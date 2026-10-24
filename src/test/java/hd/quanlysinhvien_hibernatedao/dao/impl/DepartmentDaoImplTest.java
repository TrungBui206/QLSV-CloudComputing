
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.entity.Department;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DepartmentDaoImplTest {

    @Autowired
    private DepartmentDaoImpl departmentDao;

    @Autowired
    private EntityManager entityManager;

    private Department saveDepartment(String code, String name) {
        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setDescription("Test department");

        Department saved = departmentDao.create(department);

        entityManager.flush();
        entityManager.clear();

        return saved;
    }

    @Test
    void create_success() {
        Department saved = saveDepartment(
                "CSE",
                "Computer Science"
        );

        assertNotNull(saved.getId());

        Department found = departmentDao.get(saved.getId());

        assertNotNull(found);
        assertEquals("CSE", found.getCode());
        assertEquals("Computer Science", found.getName());
    }

    @Test
    void get_notFound() {
        Department result = departmentDao.get(-1L);

        assertNull(result);
    }

    @Test
    void existsByCode_true() {
        saveDepartment("CSE", "Computer Science");

        boolean result = departmentDao.existsByCode("CSE");

        assertTrue(result);
    }

    @Test
    void existsByCode_false() {
        boolean result = departmentDao.existsByCode(
                "UNKNOWN_CODE"
        );

        assertFalse(result);
    }

    @Test
    void existsByCodeAndIdNot_true() {
        Department first = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Department second = saveDepartment(
                "IT",
                "Information Technology"
        );

        boolean result = departmentDao.existsByCodeAndIdNot(
                "CSE",
                second.getId()
        );

        assertTrue(result);
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void existsByCodeAndIdNot_false() {
        Department saved = saveDepartment(
                "CSE",
                "Computer Science"
        );

        boolean result = departmentDao.existsByCodeAndIdNot(
                "CSE",
                saved.getId()
        );

        assertFalse(result);
    }

    @Test
    void search_byName() {
        saveDepartment("CSE", "Computer Science");
        saveDepartment("BUS", "Business Administration");

        Page<Department> result = departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "computer",
                        "computer",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("name").ascending()
                        )
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "CSE",
                result.getContent().get(0).getCode()
        );
    }

    @Test
    void search_byCode() {
        saveDepartment("CSE", "Computer Science");
        saveDepartment("BUS", "Business Administration");

        Page<Department> result = departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "cse",
                        "cse",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("code").ascending()
                        )
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "CSE",
                result.getContent().get(0).getCode()
        );
    }

    @Test
    void search_caseInsensitive() {
        saveDepartment("CSE", "Computer Science");

        Page<Department> result = departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "COMPUTER",
                        "COMPUTER",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_pagination() {
        saveDepartment("CSE1", "Computer Science A");
        saveDepartment("CSE2", "Computer Science B");
        saveDepartment("CSE3", "Computer Science C");

        Page<Department> result = departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "computer",
                        "computer",
                        PageRequest.of(
                                0,
                                2,
                                Sort.by("name").ascending()
                        )
                );

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void search_sortDescending() {
        saveDepartment("CSE1", "Computer Science A");
        saveDepartment("CSE2", "Computer Science B");

        Page<Department> result = departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "computer",
                        "computer",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("name").descending()
                        )
                );

        assertEquals(2, result.getTotalElements());

        assertEquals(
                "Computer Science B",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void search_invalidSortField() {
        InvalidDataAccessApiUsageException exception = assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> departmentDao
                        .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                                "",
                                "",
                                PageRequest.of(
                                        0,
                                        10,
                                        Sort.by("invalidField")
                                )
                        )
        );

        assertInstanceOf(
                IllegalArgumentException.class,
                exception.getCause()
        );

        assertTrue(
                exception.getMessage().contains(
                        "Trường sắp xếp không hợp lệ: invalidField"
                )
        );
    }

    @Test
    void update_success() {
        Department saved = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Department existing = departmentDao.get(saved.getId());

        assertNotNull(existing);

        existing.setName("Advanced Computer Science");
        existing.setDescription("Updated department");

        departmentDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        Department updated = departmentDao.get(saved.getId());

        assertNotNull(updated);

        assertEquals(
                "Advanced Computer Science",
                updated.getName()
        );

        assertEquals(
                "Updated department",
                updated.getDescription()
        );
    }

    @Test
    void delete_success() {
        Department saved = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long id = saved.getId();

        departmentDao.deleteByPk(id);

        entityManager.flush();
        entityManager.clear();

        Department result = departmentDao.get(id);

        assertNull(result);
    }

    @Test
    void findAllSorted_success() {
        saveDepartment("BUS", "Business Administration");
        saveDepartment("CSE", "Computer Science");

        List<Department> result = departmentDao.findAllSorted(
                "name",
                "asc"
        );

        assertTrue(result.size() >= 2);

        for (int i = 1; i < result.size(); i++) {
            assertTrue(
                    result.get(i - 1).getName()
                            .compareTo(result.get(i).getName()) <= 0
            );
        }
    }
}
