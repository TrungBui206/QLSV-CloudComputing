
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;

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
class DepartmentServiceImplTest {

    @Mock
    private IDepartmentDao departmentDao;

    @Mock
    private IStudentDao studentDao;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentRequest request;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setCode("CSE");
        department.setName("Computer Science");
        department.setDescription("Computer Science Department");

        request = mock(DepartmentRequest.class);

        lenient().when(request.code()).thenReturn("CSE");
        lenient().when(request.name()).thenReturn("Computer Science");
        lenient().when(request.description())
                .thenReturn("Computer Science Department");
    }

    @Test
    void findAll_success() {
        when(departmentDao.findAllSorted("name", "asc"))
                .thenReturn(List.of(department));

        List<DepartmentResponse> result = departmentService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(departmentDao).findAllSorted("name", "asc");
    }

    @Test
    void findAll_empty() {
        when(departmentDao.findAllSorted("name", "asc"))
                .thenReturn(List.of());

        List<DepartmentResponse> result = departmentService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_success() {
        when(departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        eq("computer"),
                        eq("computer"),
                        any(Pageable.class)
                ))
                .thenReturn(new PageImpl<>(List.of(department)));

        PageResponse<DepartmentResponse> result =
                departmentService.search(
                        "computer",
                        0,
                        10,
                        "name",
                        "asc"
                );

        assertNotNull(result);

        verify(departmentDao)
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        eq("computer"),
                        eq("computer"),
                        any(Pageable.class)
                );
    }

    @Test
    void findById_success() {
        when(departmentDao.get(1L)).thenReturn(department);

        DepartmentResponse result = departmentService.findById(1L);

        assertNotNull(result);

        verify(departmentDao).get(1L);
    }

    @Test
    void findById_notFound() {
        when(departmentDao.get(99L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.findById(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void create_success() {
        when(departmentDao.existsByCode("CSE"))
                .thenReturn(false);

        when(departmentDao.create(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponse result = departmentService.create(request);

        assertNotNull(result);

        ArgumentCaptor<Department> captor =
                ArgumentCaptor.forClass(Department.class);

        verify(departmentDao).create(captor.capture());

        Department saved = captor.getValue();

        assertEquals("CSE", saved.getCode());
        assertEquals("Computer Science", saved.getName());
        assertEquals(
                "Computer Science Department",
                saved.getDescription()
        );
    }

    @Test
    void create_duplicateCode() {
        when(departmentDao.existsByCode("CSE"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.create(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(departmentDao, never())
                .create(any(Department.class));
    }

    @Test
    void update_success() {
        when(departmentDao.get(1L)).thenReturn(department);

        when(departmentDao.existsByCodeAndIdNot("CSE", 1L))
                .thenReturn(false);

        when(departmentDao.update(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponse result =
                departmentService.update(1L, request);

        assertNotNull(result);

        verify(departmentDao).update(department);

        assertEquals("CSE", department.getCode());
        assertEquals("Computer Science", department.getName());
    }

    @Test
    void update_duplicateCode() {
        when(departmentDao.get(1L)).thenReturn(department);

        when(departmentDao.existsByCodeAndIdNot("CSE", 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.update(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(departmentDao, never())
                .update(any(Department.class));
    }

    @Test
    void update_notFound() {
        when(departmentDao.get(99L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.update(99L, request)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(departmentDao, never())
                .update(any(Department.class));
    }

    @Test
    void delete_success() {
        when(departmentDao.get(1L)).thenReturn(department);

        when(studentDao.existsByDepartmentId(1L))
                .thenReturn(false);

        departmentService.delete(1L);

        verify(departmentDao).deleteByPk(1L);
    }

    @Test
    void delete_hasStudents() {
        when(departmentDao.get(1L)).thenReturn(department);

        when(studentDao.existsByDepartmentId(1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.delete(1L)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(departmentDao, never()).deleteByPk(1L);
    }

    @Test
    void delete_notFound() {
        when(departmentDao.get(99L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.delete(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(departmentDao, never()).deleteByPk(anyLong());
    }
}
