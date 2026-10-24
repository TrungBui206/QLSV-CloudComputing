
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private IStudentDao studentDao;

    @Mock
    private IDepartmentDao departmentDao;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private Department department;
    private StudentRequest request;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setCode("CSE");
        department.setName("Computer Science");

        student = new Student();
        student.setName("Nguyen Van A");
        student.setEmail("student@example.com");
        student.setDepartment(department);

        request = mock(StudentRequest.class);

        lenient().when(request.name())
                .thenReturn("Nguyen Van A");

        lenient().when(request.email())
                .thenReturn("student@example.com");

        lenient().when(request.departmentId())
                .thenReturn(10L);
    }

    @Test
    void findAll_success() {
        when(studentDao.getAll())
                .thenReturn(List.of(student));

        List<StudentResponse> result = studentService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(studentDao).getAll();
    }

    @Test
    void findAll_empty() {
        when(studentDao.getAll())
                .thenReturn(List.of());

        List<StudentResponse> result = studentService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(studentDao).getAll();
    }

    @Test
    void search_success() {
        when(studentDao.search(
                eq("nguyen"),
                eq(10L),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(student)));

        PageResponse<StudentResponse> result =
                studentService.search(
                        "nguyen",
                        10L,
                        0,
                        10,
                        "name",
                        "asc"
                );

        assertNotNull(result);

        verify(studentDao).search(
                eq("nguyen"),
                eq(10L),
                any(Pageable.class)
        );
    }

    @Test
    void search_empty() {
        when(studentDao.search(
                eq("unknown"),
                isNull(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        PageResponse<StudentResponse> result =
                studentService.search(
                        "unknown",
                        null,
                        0,
                        10,
                        "name",
                        "asc"
                );

        assertNotNull(result);

        verify(studentDao).search(
                eq("unknown"),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void findById_success() {
        when(studentDao.get(1L))
                .thenReturn(student);

        StudentResponse result = studentService.findById(1L);

        assertNotNull(result);

        verify(studentDao).get(1L);
    }

    @Test
    void findById_notFound() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.findById(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(studentDao).get(99L);
    }

    @Test
    void create_success() {
        when(departmentDao.get(10L))
                .thenReturn(department);

        when(studentDao.create(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse result = studentService.create(request);

        assertNotNull(result);

        ArgumentCaptor<Student> captor =
                ArgumentCaptor.forClass(Student.class);

        verify(studentDao).existsByEmail("student@example.com");
        verify(studentDao).create(captor.capture());

        Student saved = captor.getValue();

        assertEquals("Nguyen Van A", saved.getName());
        assertEquals("student@example.com", saved.getEmail());
        assertSame(department, saved.getDepartment());
    }

    @Test
    void create_duplicateEmail() {
        when(studentDao.existsByEmail("student@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.create(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(studentDao, never())
                .create(any(Student.class));

        verifyNoInteractions(departmentDao);
    }

    @Test
    void create_departmentNotFound() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.create(request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(departmentDao).get(10L);

        verify(studentDao, never())
                .create(any(Student.class));
    }

    @Test
    void update_success() {
        when(studentDao.get(1L))
                .thenReturn(student);

        when(departmentDao.get(10L))
                .thenReturn(department);

        when(studentDao.update(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse result =
                studentService.update(1L, request);

        assertNotNull(result);

        verify(studentDao).existsByEmailAndIdNot(
                "student@example.com",
                1L
        );

        verify(studentDao).update(student);

        assertEquals("Nguyen Van A", student.getName());
        assertEquals("student@example.com", student.getEmail());
        assertSame(department, student.getDepartment());
    }

    @Test
    void update_duplicateEmail() {
        when(studentDao.get(1L))
                .thenReturn(student);

        when(studentDao.existsByEmailAndIdNot(
                "student@example.com",
                1L
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.update(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(studentDao, never())
                .update(any(Student.class));

        verifyNoInteractions(departmentDao);
    }

    @Test
    void update_notFound() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.update(99L, request)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(studentDao).get(99L);

        verify(studentDao, never())
                .update(any(Student.class));
    }

    @Test
    void update_departmentNotFound() {
        when(studentDao.get(1L))
                .thenReturn(student);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.update(1L, request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(departmentDao).get(10L);

        verify(studentDao, never())
                .update(any(Student.class));
    }

    @Test
    void delete_success() {
        when(studentDao.exists(1L))
                .thenReturn(true);

        studentService.delete(1L);

        verify(studentDao).exists(1L);
        verify(studentDao).deleteByPk(1L);
    }

    @Test
    void delete_notFound() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> studentService.delete(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(studentDao).exists(99L);
        verify(studentDao, never()).deleteByPk(anyLong());
    }
}
