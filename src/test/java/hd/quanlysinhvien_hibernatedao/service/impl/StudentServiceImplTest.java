package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import hd.quanlysinhvien_hibernatedao.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final StudentMapper studentMapper =
            Mappers.getMapper(StudentMapper.class);

    private StudentServiceImpl studentService;
    private Student student;
    private Department department;
    private StudentRequest request;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(
                studentDao, departmentDao, studentMapper
        );

        department = new Department();
        department.setId(10L);
        department.setCode("CSE");
        department.setName("Computer Science");

        student = new Student();
        student.setId(1L);
        student.setName("Nguyen Van A");
        student.setEmail("student@example.com");
        student.setDob(LocalDate.of(2004, 1, 15));
        student.setDepartment(department);

        request = new StudentRequest(
                "Nguyen Van A",
                "student@example.com",
                LocalDate.of(2004, 1, 15),
                10L
        );
    }

    @Test
    void findAll_success() {
        when(studentDao.getAll()).thenReturn(List.of(student));
        List<StudentResponse> result = studentService.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Nguyen Van A", result.get(0).name());
        assertEquals(10L, result.get(0).departmentId());
        assertEquals("Computer Science", result.get(0).departmentName());
        verify(studentDao).getAll();
    }

    @Test
    void findAll_empty() {
        when(studentDao.getAll()).thenReturn(List.of());
        assertTrue(studentService.findAll().isEmpty());
    }

    @Test
    void search_success() {
        when(studentDao.search(eq("nguyen"), eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student)));
        PageResponse<StudentResponse> result =
                studentService.search("nguyen", 10L, 0, 10, "name", "asc");

        assertEquals(1, result.content().size());
        assertEquals(1L, result.content().get(0).id());
        verify(studentDao).search(eq("nguyen"), eq(10L), any(Pageable.class));
    }

    @Test
    void search_empty() {
        when(studentDao.search(eq("unknown"), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        PageResponse<StudentResponse> result =
                studentService.search("unknown", null, 0, 10, "name", "asc");
        assertTrue(result.content().isEmpty());
    }

    @Test
    void findById_success() {
        when(studentDao.get(1L)).thenReturn(student);
        StudentResponse result = studentService.findById(1L);
        assertEquals(1L, result.id());
        assertEquals("Nguyen Van A", result.name());
        assertEquals(LocalDate.of(2004, 1, 15), result.dob());
        assertEquals(10L, result.departmentId());
    }

    @Test
    void findById_notFound() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.findById(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(studentDao).get(99L);
    }

    @Test
    void create_success() {
        when(departmentDao.get(10L)).thenReturn(department);
        when(studentDao.create(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse result = studentService.create(request);
        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentDao).existsByEmail("student@example.com");
        verify(departmentDao).get(10L);
        verify(studentDao).create(captor.capture());

        Student saved = captor.getValue();
        assertNull(saved.getId());
        assertEquals("Nguyen Van A", saved.getName());
        assertEquals("student@example.com", saved.getEmail());
        assertEquals(LocalDate.of(2004, 1, 15), saved.getDob());
        assertSame(department, saved.getDepartment());
        assertEquals(10L, result.departmentId());
        assertEquals("Computer Science", result.departmentName());
    }

    @Test
    void create_duplicateEmail() {
        when(studentDao.existsByEmail("student@example.com"))
                .thenReturn(true);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.create(request)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(studentDao, never()).create(any(Student.class));
        verifyNoInteractions(departmentDao);
    }

    @Test
    void create_departmentNotFound() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.create(request)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(departmentDao).get(10L);
        verify(studentDao, never()).create(any(Student.class));
    }

    @Test
    void update_success() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime updated = LocalDateTime.of(2026, 1, 2, 9, 0);
        student.setCreatedAt(created);
        student.setUpdatedAt(updated);
        StudentRequest changed = new StudentRequest(
                "Tran Van B", "new@example.com",
                LocalDate.of(2005, 2, 3), 10L
        );
        when(studentDao.get(1L)).thenReturn(student);
        when(departmentDao.get(10L)).thenReturn(department);
        when(studentDao.update(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse result = studentService.update(1L, changed);

        verify(studentDao).existsByEmailAndIdNot("new@example.com", 1L);
        verify(departmentDao).get(10L);
        verify(studentDao).update(student);
        assertEquals(1L, student.getId());
        assertEquals("Tran Van B", student.getName());
        assertEquals("new@example.com", student.getEmail());
        assertEquals(LocalDate.of(2005, 2, 3), student.getDob());
        assertSame(department, student.getDepartment());
        assertEquals(created, student.getCreatedAt());
        assertEquals(updated, student.getUpdatedAt());
        assertEquals(1L, result.id());
        assertEquals("Tran Van B", result.name());
    }

    @Test
    void update_duplicateEmail() {
        when(studentDao.get(1L)).thenReturn(student);
        when(studentDao.existsByEmailAndIdNot("student@example.com", 1L))
                .thenReturn(true);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.update(1L, request)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(studentDao, never()).update(any(Student.class));
        verifyNoInteractions(departmentDao);
    }

    @Test
    void update_notFound() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.update(99L, request)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(studentDao).get(99L);
        verify(studentDao, never()).update(any(Student.class));
    }

    @Test
    void update_departmentNotFound() {
        when(studentDao.get(1L)).thenReturn(student);
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.update(1L, request)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(departmentDao).get(10L);
        verify(studentDao, never()).update(any(Student.class));
    }

    @Test
    void delete_success() {
        when(studentDao.exists(1L)).thenReturn(true);
        studentService.delete(1L);
        verify(studentDao).exists(1L);
        verify(studentDao).deleteByPk(1L);
    }

    @Test
    void delete_notFound() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.delete(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(studentDao).exists(99L);
        verify(studentDao, never()).deleteByPk(anyLong());
    }

    @Test
    void search_invalidPage() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.search("", null, -1, 10, "name", "asc")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(studentDao, departmentDao);
    }

    @Test
    void search_invalidSortField() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.search("", null, 0, 10, "unknownField", "asc")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(studentDao, departmentDao);
    }
}
