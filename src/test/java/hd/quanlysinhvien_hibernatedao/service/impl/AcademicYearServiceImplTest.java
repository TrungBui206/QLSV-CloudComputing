
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IAcademicYearDao;
import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;

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
class AcademicYearServiceImplTest {

    @Mock
    private IAcademicYearDao academicYearDao;

    @InjectMocks
    private AcademicYearServiceImpl academicYearService;

    private AcademicYear academicYear;
    private AcademicYearRequest request;

    @BeforeEach
    void setUp() {
        academicYear = new AcademicYear();
        academicYear.setCode("AY2026");
        academicYear.setName("Academic Year 2026-2027");
        academicYear.setDescription("Academic year for 2026-2027");
        academicYear.setStartYear(2026);
        academicYear.setEndYear(2027);

        request = mock(AcademicYearRequest.class);

        lenient().when(request.code()).thenReturn("AY2026");
        lenient().when(request.name())
                .thenReturn("Academic Year 2026-2027");
        lenient().when(request.description())
                .thenReturn("Academic year for 2026-2027");
        lenient().when(request.startYear()).thenReturn(2026);
        lenient().when(request.endYear()).thenReturn(2027);
    }

    @Test
    void search_success() {
        when(academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        eq("2026"),
                        eq("2026"),
                        any(Pageable.class)
                ))
                .thenReturn(new PageImpl<>(List.of(academicYear)));

        PageResponse<AcademicYearResponse> result =
                academicYearService.search(
                        "2026",
                        0,
                        10,
                        "startYear",
                        "asc"
                );

        assertNotNull(result);

        verify(academicYearDao)
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        eq("2026"),
                        eq("2026"),
                        any(Pageable.class)
                );
    }

    @Test
    void search_empty() {
        when(academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        eq("unknown"),
                        eq("unknown"),
                        any(Pageable.class)
                ))
                .thenReturn(new PageImpl<>(List.of()));

        PageResponse<AcademicYearResponse> result =
                academicYearService.search(
                        "unknown",
                        0,
                        10,
                        "name",
                        "asc"
                );

        assertNotNull(result);
    }

    @Test
    void findById_success() {
        when(academicYearDao.get(1L))
                .thenReturn(academicYear);

        AcademicYearResponse result =
                academicYearService.findById(1L);

        assertNotNull(result);

        verify(academicYearDao).get(1L);
    }

    @Test
    void findById_notFound() {
        when(academicYearDao.get(99L))
                .thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.findById(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void create_success() {
        when(academicYearDao.create(any(AcademicYear.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcademicYearResponse result =
                academicYearService.create(request);

        assertNotNull(result);

        ArgumentCaptor<AcademicYear> captor =
                ArgumentCaptor.forClass(AcademicYear.class);

        verify(academicYearDao).existsByCode("AY2026");
        verify(academicYearDao).create(captor.capture());

        AcademicYear saved = captor.getValue();

        assertEquals("AY2026", saved.getCode());
        assertEquals("Academic Year 2026-2027", saved.getName());
        assertEquals(2026, saved.getStartYear());
        assertEquals(2027, saved.getEndYear());
    }

    @Test
    void create_duplicateCode() {
        when(academicYearDao.existsByCode("AY2026"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.create(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(academicYearDao, never())
                .create(any(AcademicYear.class));
    }

    @Test
    void create_invalidYears() {
        when(request.startYear()).thenReturn(2027);
        when(request.endYear()).thenReturn(2026);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.create(request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(academicYearDao);
    }

    @Test
    void create_equalYears() {
        when(request.startYear()).thenReturn(2026);
        when(request.endYear()).thenReturn(2026);

        when(academicYearDao.create(any(AcademicYear.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcademicYearResponse result =
                academicYearService.create(request);

        assertNotNull(result);

        verify(academicYearDao).create(any(AcademicYear.class));
    }

    @Test
    void update_success() {
        when(academicYearDao.get(1L))
                .thenReturn(academicYear);

        when(academicYearDao.update(any(AcademicYear.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcademicYearResponse result =
                academicYearService.update(1L, request);

        assertNotNull(result);

        verify(academicYearDao).existsByCodeAndIdNot("AY2026", 1L);
        verify(academicYearDao).update(academicYear);

        assertEquals(2026, academicYear.getStartYear());
        assertEquals(2027, academicYear.getEndYear());
    }

    @Test
    void update_duplicateCode() {
        when(academicYearDao.get(1L))
                .thenReturn(academicYear);

        when(academicYearDao.existsByCodeAndIdNot("AY2026", 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.update(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(academicYearDao, never())
                .update(any(AcademicYear.class));
    }

    @Test
    void update_invalidYears() {
        when(academicYearDao.get(1L))
                .thenReturn(academicYear);

        when(request.startYear()).thenReturn(2028);
        when(request.endYear()).thenReturn(2027);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.update(1L, request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(academicYearDao, never())
                .update(any(AcademicYear.class));
    }

    @Test
    void update_notFound() {
        when(academicYearDao.get(99L))
                .thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.update(99L, request)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(academicYearDao, never())
                .update(any(AcademicYear.class));
    }

    @Test
    void delete_success() {
        when(academicYearDao.exists(1L))
                .thenReturn(true);

        academicYearService.delete(1L);

        verify(academicYearDao).exists(1L);
        verify(academicYearDao).deleteByPk(1L);
    }

    @Test
    void delete_notFound() {
        when(academicYearDao.exists(99L))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.delete(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(academicYearDao, never()).deleteByPk(anyLong());
    }
}
