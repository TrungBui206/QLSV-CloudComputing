
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IClassInfoDao;
import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassInfoServiceImplTest {

    @Mock
    private IClassInfoDao classInfoDao;

    @InjectMocks
    private ClassInfoServiceImpl classInfoService;

    private ClassInfo classInfo;
    private ClassInfoRequest request;

    @BeforeEach
    void setUp() {
        classInfo = mock(ClassInfo.class);

        lenient().when(classInfo.getId())
                .thenReturn("class-001");

        lenient().when(classInfo.getName())
                .thenReturn("CSE2026");

        request = mock(ClassInfoRequest.class);

        lenient().when(request.id())
                .thenReturn("class-001");

        lenient().when(request.name())
                .thenReturn("CSE2026");

        lenient().when(request.domain())
                .thenReturn("Computer Science");

        lenient().when(request.description())
                .thenReturn("Lop khoa hoc may tinh");
    }

    @Test
    void create_success() {
        when(classInfoDao.findByNameIgnoreCase("CSE2026"))
                .thenReturn(Optional.empty());

        when(classInfoDao.create(any(ClassInfo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClassInfoResponse result = classInfoService.create(request);

        assertNotNull(result);

        ArgumentCaptor<ClassInfo> captor =
                ArgumentCaptor.forClass(ClassInfo.class);

        verify(classInfoDao).create(captor.capture());
        verify(classInfoDao).flush();

        ClassInfo saved = captor.getValue();

        assertEquals("CSE2026", saved.getName());
        assertEquals("Computer Science", saved.getDomain());
        assertEquals("Lop khoa hoc may tinh", saved.getDescription());
    }

    @Test
    void create_duplicateName() {
        when(classInfoDao.findByNameIgnoreCase("CSE2026"))
                .thenReturn(Optional.of(classInfo));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(classInfoDao, never())
                .create(any(ClassInfo.class));
    }

    @Test
    void create_nullRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void create_blankName() {
        when(request.name()).thenReturn("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void create_nullDomain() {
        when(request.domain()).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void create_nameTooLong() {
        when(request.name()).thenReturn("A".repeat(151));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void create_domainTooLong() {
        when(request.domain()).thenReturn("A".repeat(121));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void create_descriptionTooLong() {
        when(request.description()).thenReturn("A".repeat(1001));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void create_trimValues() {
        when(request.name()).thenReturn("  CSE2026  ");
        when(request.domain()).thenReturn("  Computer Science  ");
        when(request.description()).thenReturn("  Description  ");

        when(classInfoDao.create(any(ClassInfo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        classInfoService.create(request);

        ArgumentCaptor<ClassInfo> captor =
                ArgumentCaptor.forClass(ClassInfo.class);

        verify(classInfoDao).findByNameIgnoreCase("CSE2026");
        verify(classInfoDao).create(captor.capture());

        ClassInfo saved = captor.getValue();

        assertEquals("CSE2026", saved.getName());
        assertEquals("Computer Science", saved.getDomain());
        assertEquals("Description", saved.getDescription());
    }

    @Test
    void create_blankDescription() {
        when(request.description()).thenReturn("   ");

        when(classInfoDao.create(any(ClassInfo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        classInfoService.create(request);

        ArgumentCaptor<ClassInfo> captor =
                ArgumentCaptor.forClass(ClassInfo.class);

        verify(classInfoDao).create(captor.capture());

        assertNull(captor.getValue().getDescription());
    }

    @Test
    void update_success() {
        when(classInfoDao.findById("class-001"))
                .thenReturn(Optional.of(classInfo));

        when(classInfoDao.findByNameIgnoreCase("CSE2026"))
                .thenReturn(Optional.empty());

        when(classInfoDao.update(classInfo))
                .thenReturn(classInfo);

        ClassInfoResponse result = classInfoService.update(request);

        assertNotNull(result);

        verify(classInfoDao).update(classInfo);
        verify(classInfoDao).flush();

        verify(classInfo).setName("CSE2026");
        verify(classInfo).setDomain("Computer Science");
        verify(classInfo).setDescription("Lop khoa hoc may tinh");
    }

    @Test
    void update_sameNameSameId() {
        when(classInfoDao.findById("class-001"))
                .thenReturn(Optional.of(classInfo));

        when(classInfoDao.findByNameIgnoreCase("CSE2026"))
                .thenReturn(Optional.of(classInfo));

        when(classInfoDao.update(classInfo))
                .thenReturn(classInfo);

        ClassInfoResponse result = classInfoService.update(request);

        assertNotNull(result);

        verify(classInfoDao).update(classInfo);
        verify(classInfoDao).flush();
    }

    @Test
    void update_duplicateName() {
        ClassInfo duplicate = mock(ClassInfo.class);

        when(duplicate.getId()).thenReturn("class-002");

        when(classInfoDao.findById("class-001"))
                .thenReturn(Optional.of(classInfo));

        when(classInfoDao.findByNameIgnoreCase("CSE2026"))
                .thenReturn(Optional.of(duplicate));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.update(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(classInfoDao, never())
                .update(any(ClassInfo.class));
    }

    @Test
    void update_notFound() {
        when(classInfoDao.findById("class-001"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.update(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classInfoDao, never())
                .update(any(ClassInfo.class));
    }

    @Test
    void update_blankId() {
        when(request.id()).thenReturn("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.update(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void delete_success() {
        when(classInfoDao.findById("class-001"))
                .thenReturn(Optional.of(classInfo));

        classInfoService.delete("class-001");

        verify(classInfoDao).delete(classInfo);
        verify(classInfoDao).flush();
    }

    @Test
    void delete_notFound() {
        when(classInfoDao.findById("class-999"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.delete("class-999")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(classInfoDao, never())
                .delete(any(ClassInfo.class));
    }

    @Test
    void delete_blankId() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.delete("   ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void getById_success() {
        when(classInfoDao.findById("class-001"))
                .thenReturn(Optional.of(classInfo));

        ClassInfoResponse result =
                classInfoService.getById("class-001");

        assertNotNull(result);

        verify(classInfoDao).findById("class-001");
    }

    @Test
    void getById_notFound() {
        when(classInfoDao.findById("class-999"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.getById("class-999")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getByName_success() {
        when(classInfoDao.findByNameIgnoreCase("CSE2026"))
                .thenReturn(Optional.of(classInfo));

        ClassInfoResponse result =
                classInfoService.getByName("CSE2026");

        assertNotNull(result);

        verify(classInfoDao).findByNameIgnoreCase("CSE2026");
    }

    @Test
    void getByName_notFound() {
        when(classInfoDao.findByNameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.getByName("Unknown")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getAll_success() {
        when(classInfoDao.getAll())
                .thenReturn(List.of(classInfo));

        List<ClassInfoResponse> result = classInfoService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(classInfoDao).getAll();
    }

    @Test
    void getAll_empty() {
        when(classInfoDao.getAll())
                .thenReturn(List.of());

        List<ClassInfoResponse> result = classInfoService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getByDomain_success() {
        when(classInfoDao.findByDomainIgnoreCase("Computer Science"))
                .thenReturn(List.of(classInfo));

        List<ClassInfoResponse> result =
                classInfoService.getByDomain("Computer Science");

        assertEquals(1, result.size());

        verify(classInfoDao)
                .findByDomainIgnoreCase("Computer Science");
    }

    @Test
    void getByDomain_blank() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.getByDomain("   ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void search_success() {
        when(classInfoDao.search(
                "CSE", 0, 10, "name", "asc"
        )).thenReturn(List.of(classInfo));

        List<ClassInfoResponse> result =
                classInfoService.search(
                        "CSE", 0, 10, "name", "asc"
                );

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(classInfoDao)
                .search("CSE", 0, 10, "name", "asc");
    }

    @Test
    void search_defaultPagination() {
        when(classInfoDao.search(
                "CSE", 0, 10, "name", "asc"
        )).thenReturn(List.of());

        List<ClassInfoResponse> result =
                classInfoService.search(
                        "CSE", null, null, "name", "asc"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(classInfoDao)
                .search("CSE", 0, 10, "name", "asc");
    }

    @Test
    void search_negativeLowerLimit() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.search(
                        "CSE", -1, 10, "name", "asc"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void search_invalidRange() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.search(
                        "CSE", 10, 5, "name", "asc"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void search_limitExceeded() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> classInfoService.search(
                        "CSE", 0, 101, "name", "asc"
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verifyNoInteractions(classInfoDao);
    }

    @Test
    void getCount_success() {
        when(classInfoDao.countSearch("CSE"))
                .thenReturn(5L);

        Integer result = classInfoService.getCount("CSE");

        assertEquals(5, result);

        verify(classInfoDao).countSearch("CSE");
    }

    @Test
    void getCount_overflow() {
        when(classInfoDao.countSearch("CSE"))
                .thenReturn((long) Integer.MAX_VALUE + 1);

        assertThrows(
                ArithmeticException.class,
                () -> classInfoService.getCount("CSE")
        );
    }
}
