package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.service.AcademicYearService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AcademicYearFlowIntegrationTest {

    @Autowired
    private AcademicYearService academicYearService;

    private AcademicYearRequest request(String code) {
        return AcademicYearRequest.builder()
                .code(code)
                .name("Academic Year 2026-2027")
                .description("Niên khóa kiểm thử tích hợp")
                .startYear(2026)
                .endYear(2027)
                .build();
    }

    @Test
    void createAndRead_roundTrip() {
        AcademicYearResponse created = academicYearService.create(request("AY_ROUNDTRIP"));
        assertNotNull(created.id());
        assertEquals("AY_ROUNDTRIP", created.code());
        assertEquals(2026, created.startYear());
        assertEquals(2027, created.endYear());

        AcademicYearResponse found = academicYearService.findById(created.id());
        assertEquals(created.id(), found.id());
        assertEquals(created.name(), found.name());
    }

    @Test
    void update_preservesIdAndChangesFields() {
        AcademicYearResponse created = academicYearService.create(request("AY_BEFORE"));
        AcademicYearRequest changed = request("AY_AFTER").toBuilder()
                .name("Updated Academic Year")
                .startYear(2027)
                .endYear(2028)
                .build();

        AcademicYearResponse updated = academicYearService.update(created.id(), changed);
        assertEquals(created.id(), updated.id());
        assertEquals("AY_AFTER", updated.code());
        assertEquals("Updated Academic Year", updated.name());
        assertEquals(2027, updated.startYear());
        assertEquals(2028, updated.endYear());
        assertEquals(created.id(), academicYearService.findById(created.id()).id());
    }

    @Test
    void search_returnsMatchingAcademicYear() {
        academicYearService.create(request("AY_FINDME"));
        PageResponse<AcademicYearResponse> page = academicYearService.search(
                "AY_FINDME", 0, 10, "code", "asc"
        );
        assertEquals(1L, page.totalElements());
        assertEquals(1, page.content().size());
        assertEquals("AY_FINDME", page.content().get(0).code());
    }

    @Test
    void delete_removesAcademicYear() {
        AcademicYearResponse created = academicYearService.create(request("AY_DELETE"));
        academicYearService.delete(created.id());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.findById(created.id())
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void duplicateCode_returnsConflict() {
        academicYearService.create(request("AY_DUPLICATE"));
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.create(request("AY_DUPLICATE"))
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void invalidYearRange_returnsBadRequest() {
        AcademicYearRequest invalid = request("AY_INVALID").toBuilder()
                .startYear(2028)
                .endYear(2027)
                .build();
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> academicYearService.create(invalid)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void equalYears_areAcceptedLikeOriginalService() {
        AcademicYearRequest equal = request("AY_EQUAL").toBuilder()
                .startYear(2026)
                .endYear(2026)
                .build();
        AcademicYearResponse result = academicYearService.create(equal);
        assertNotNull(result.id());
        assertEquals(2026, result.startYear());
        assertEquals(2026, result.endYear());
    }
}
