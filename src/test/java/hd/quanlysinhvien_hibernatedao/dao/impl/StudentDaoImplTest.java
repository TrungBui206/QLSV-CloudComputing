
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;

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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentDaoImplTest {

    @Autowired
    private StudentDaoImpl studentDao;

    @Autowired
    private EntityManager entityManager;

    private Long saveDepartment(String code, String name) {
        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        department.setDescription("Test department");

        entityManager.persist(department);
        entityManager.flush();

        Long id = department.getId();

        entityManager.clear();

        return id;
    }

    private Long saveStudent(
            String name,
            String email,
            Long departmentId
    ) {
        Student student = new Student();

        student.setName(name);
        student.setEmail(email);
        student.setDob(LocalDate.of(2004, 1, 15));

        Department department = entityManager.getReference(
                Department.class,
                departmentId
        );

        student.setDepartment(department);

        Student saved = studentDao.create(student);

        entityManager.flush();

        Long id = saved.getId();

        entityManager.clear();

        return id;
    }

    @Test
    void findByEmailIgnoreCase_found() {
        Long departmentId = saveDepartment(
                "SCP",
                "Scope Department"
        );

        Long studentId = saveStudent(
                "Scope Student",
                "Scope.Student@Example.com",
                departmentId
        );

        Optional<Student> result =
                studentDao.findByEmailIgnoreCase("  scope.student@example.COM ");

        assertTrue(result.isPresent());
        assertEquals(studentId, result.get().getId());
        assertEquals(
                "Scope Department",
                result.get().getDepartment().getName()
        );
    }

    @Test
    void findByEmailIgnoreCase_notFoundOrBlank() {
        assertTrue(studentDao.findByEmailIgnoreCase("nobody@example.com").isEmpty());
        assertTrue(studentDao.findByEmailIgnoreCase("   ").isEmpty());
        assertTrue(studentDao.findByEmailIgnoreCase(null).isEmpty());
    }

    @Test
    void create_success() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long studentId = saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        assertNotNull(studentId);

        Student found = studentDao.get(studentId);

        assertNotNull(found);
        assertEquals("Nguyen Van A", found.getName());
        assertEquals("student.a@example.com", found.getEmail());
        assertEquals(departmentId, found.getDepartment().getId());
    }

    @Test
    void get_notFound() {
        Student result = studentDao.get(-1L);

        assertNull(result);
    }

    @Test
    void existsByEmail_true() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        assertTrue(
                studentDao.existsByEmail("student.a@example.com")
        );
    }

    @Test
    void existsByEmail_false() {
        assertFalse(
                studentDao.existsByEmail("unknown@example.com")
        );
    }

    @Test
    void existsByEmailAndIdNot_true() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long firstId = saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        Long secondId = saveStudent(
                "Tran Van B",
                "student.b@example.com",
                departmentId
        );

        assertNotEquals(firstId, secondId);

        assertTrue(
                studentDao.existsByEmailAndIdNot(
                        "student.a@example.com",
                        secondId
                )
        );
    }

    @Test
    void existsByEmailAndIdNot_false() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long studentId = saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        assertFalse(
                studentDao.existsByEmailAndIdNot(
                        "student.a@example.com",
                        studentId
                )
        );
    }

    @Test
    void existsByDepartmentId_true() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        assertTrue(
                studentDao.existsByDepartmentId(departmentId)
        );
    }

    @Test
    void existsByDepartmentId_false() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        assertFalse(
                studentDao.existsByDepartmentId(departmentId)
        );
    }

    @Test
    void search_byName_caseInsensitive() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        saveStudent(
                "Tran Van B",
                "student.b@example.com",
                departmentId
        );

        Page<Student> result = studentDao.search(
                "NGUYEN",
                null,
                PageRequest.of(
                        0,
                        10,
                        Sort.by("name").ascending()
                )
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Nguyen Van A", result.getContent().get(0).getName());
    }

    @Test
    void search_byEmail() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        saveStudent(
                "Tran Van B",
                "student.b@example.com",
                departmentId
        );

        Page<Student> result = studentDao.search(
                "STUDENT.B@EXAMPLE.COM",
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "student.b@example.com",
                result.getContent().get(0).getEmail()
        );
    }

    @Test
    void search_byDepartment() {
        Long cseId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long busId = saveDepartment(
                "BUS",
                "Business Administration"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                cseId
        );

        saveStudent(
                "Tran Van B",
                "student.b@example.com",
                busId
        );

        Page<Student> result = studentDao.search(
                "",
                cseId,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                cseId,
                result.getContent().get(0).getDepartment().getId()
        );
    }

    @Test
    void search_trimKeyword() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        Page<Student> result = studentDao.search(
                "  NGUYEN  ",
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_empty() {
        Page<Student> result = studentDao.search(
                "no_matching_student_999",
                null,
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void search_pagination() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Student A",
                "student.a@example.com",
                departmentId
        );

        saveStudent(
                "Student B",
                "student.b@example.com",
                departmentId
        );

        saveStudent(
                "Student C",
                "student.c@example.com",
                departmentId
        );

        Page<Student> result = studentDao.search(
                "student",
                departmentId,
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
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Student A",
                "student.a@example.com",
                departmentId
        );

        saveStudent(
                "Student B",
                "student.b@example.com",
                departmentId
        );

        Page<Student> result = studentDao.search(
                "student",
                departmentId,
                PageRequest.of(
                        0,
                        10,
                        Sort.by("name").descending()
                )
        );

        assertEquals(2, result.getTotalElements());

        assertEquals(
                "Student B",
                result.getContent().get(0).getName()
        );
    }

    @Test
    void search_unsorted() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        Page<Student> result = studentDao.search(
                "",
                departmentId,
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_invalidSortField() {
        InvalidDataAccessApiUsageException exception = assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> studentDao.search(
                        "",
                        null,
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
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long studentId = saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        Student existing = studentDao.get(studentId);

        assertNotNull(existing);

        existing.setName("Nguyen Van B");
        existing.setEmail("student.updated@example.com");

        studentDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        Student updated = studentDao.get(studentId);

        assertNotNull(updated);
        assertEquals("Nguyen Van B", updated.getName());

        assertEquals(
                "student.updated@example.com",
                updated.getEmail()
        );
    }

    @Test
    void delete_success() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        Long studentId = saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        studentDao.deleteByPk(studentId);

        entityManager.flush();
        entityManager.clear();

        assertNull(studentDao.get(studentId));

        assertFalse(
                studentDao.existsByEmail("student.a@example.com")
        );
    }

    @Test
    void getAll_success() {
        Long departmentId = saveDepartment(
                "CSE",
                "Computer Science"
        );

        saveStudent(
                "Nguyen Van A",
                "student.a@example.com",
                departmentId
        );

        saveStudent(
                "Tran Van B",
                "student.b@example.com",
                departmentId
        );

        List<Student> result = studentDao.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(s ->
                                "student.a@example.com".equals(s.getEmail())
                        )
        );

        assertTrue(
                result.stream()
                        .anyMatch(s ->
                                "student.b@example.com".equals(s.getEmail())
                        )
        );
    }
}
