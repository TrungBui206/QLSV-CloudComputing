
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;

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
class AcademicYearDaoImplTest {

    @Autowired
    private AcademicYearDaoImpl academicYearDao;

    @Autowired
    private EntityManager entityManager;

    private AcademicYear saveAcademicYear(
            String code,
            String name,
            int startYear,
            int endYear
    ) {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setCode(code);
        academicYear.setName(name);
        academicYear.setDescription("Test academic year");
        academicYear.setStartYear(startYear);
        academicYear.setEndYear(endYear);

        AcademicYear saved = academicYearDao.create(academicYear);

        entityManager.flush();
        entityManager.clear();

        return saved;
    }

    @Test
    void create_success() {
        AcademicYear saved = saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        assertNotNull(saved.getId());

        AcademicYear found = academicYearDao.get(saved.getId());

        assertNotNull(found);
        assertEquals("AY2026", found.getCode());
        assertEquals("Academic Year 2026-2027", found.getName());
        assertEquals(2026, found.getStartYear());
        assertEquals(2027, found.getEndYear());
    }

    @Test
    void get_notFound() {
        AcademicYear result = academicYearDao.get(-1L);

        assertNull(result);
    }

    @Test
    void existsByCode_true() {
        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        assertTrue(academicYearDao.existsByCode("AY2026"));
    }

    @Test
    void existsByCode_false() {
        assertFalse(
                academicYearDao.existsByCode("UNKNOWN_CODE")
        );
    }

    @Test
    void existsByCodeAndIdNot_true() {
        AcademicYear first = saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        AcademicYear second = saveAcademicYear(
                "AY2027",
                "Academic Year 2027-2028",
                2027,
                2028
        );

        assertNotEquals(first.getId(), second.getId());

        assertTrue(
                academicYearDao.existsByCodeAndIdNot(
                        "AY2026",
                        second.getId()
                )
        );
    }

    @Test
    void existsByCodeAndIdNot_false() {
        AcademicYear saved = saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        assertFalse(
                academicYearDao.existsByCodeAndIdNot(
                        "AY2026",
                        saved.getId()
                )
        );
    }

    @Test
    void search_byName() {
        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        saveAcademicYear(
                "AY2027",
                "Academic Year 2027-2028",
                2027,
                2028
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "2026-2027",
                        "2026-2027",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("name").ascending()
                        )
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("AY2026", result.getContent().get(0).getCode());
    }

    @Test
    void search_byCode() {
        saveAcademicYear(
                "AY2026",
                "First Academic Year",
                2026,
                2027
        );

        saveAcademicYear(
                "AY2027",
                "Second Academic Year",
                2027,
                2028
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "ay2026",
                        "ay2026",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("code").ascending()
                        )
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("AY2026", result.getContent().get(0).getCode());
    }

    @Test
    void search_caseInsensitive() {
        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "ACADEMIC YEAR 2026",
                        "ACADEMIC YEAR 2026",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_nullKeywords() {
        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        saveAcademicYear(
                "AY2027",
                "Academic Year 2027-2028",
                2027,
                2028
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        null,
                        null,
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_pagination() {
        saveAcademicYear(
                "AY2025",
                "Academic Year 2025-2026",
                2025,
                2026
        );

        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        saveAcademicYear(
                "AY2027",
                "Academic Year 2027-2028",
                2027,
                2028
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "academic",
                        "academic",
                        PageRequest.of(
                                0,
                                2,
                                Sort.by("startYear").ascending()
                        )
                );

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertEquals(2025, result.getContent().get(0).getStartYear());
    }

    @Test
    void search_secondPage() {
        saveAcademicYear(
                "AY2025",
                "Academic Year 2025-2026",
                2025,
                2026
        );

        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        saveAcademicYear(
                "AY2027",
                "Academic Year 2027-2028",
                2027,
                2028
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "academic",
                        "academic",
                        PageRequest.of(
                                1,
                                2,
                                Sort.by("startYear").ascending()
                        )
                );

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(2027, result.getContent().get(0).getStartYear());
    }

    @Test
    void search_sortDescending() {
        saveAcademicYear(
                "AY2025",
                "Academic Year 2025-2026",
                2025,
                2026
        );

        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        Page<AcademicYear> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        "academic",
                        "academic",
                        PageRequest.of(
                                0,
                                10,
                                Sort.by("startYear").descending()
                        )
                );

        assertEquals(2, result.getTotalElements());
        assertEquals(2026, result.getContent().get(0).getStartYear());
        assertEquals(2025, result.getContent().get(1).getStartYear());
    }

    @Test
    void search_invalidSortField() {
        InvalidDataAccessApiUsageException exception = assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> academicYearDao
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
        AcademicYear saved = saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        AcademicYear existing = academicYearDao.get(saved.getId());

        assertNotNull(existing);

        existing.setName("Updated Academic Year");
        existing.setDescription("Updated description");
        existing.setStartYear(2026);
        existing.setEndYear(2028);

        academicYearDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        AcademicYear updated = academicYearDao.get(saved.getId());

        assertNotNull(updated);
        assertEquals("Updated Academic Year", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(2026, updated.getStartYear());
        assertEquals(2028, updated.getEndYear());
    }

    @Test
    void delete_success() {
        AcademicYear saved = saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        Long id = saved.getId();

        academicYearDao.deleteByPk(id);

        entityManager.flush();
        entityManager.clear();

        assertNull(academicYearDao.get(id));
        assertFalse(academicYearDao.existsByCode("AY2026"));
    }

    @Test
    void findAllSorted_success() {
        saveAcademicYear(
                "AY2027",
                "Academic Year 2027-2028",
                2027,
                2028
        );

        saveAcademicYear(
                "AY2025",
                "Academic Year 2025-2026",
                2025,
                2026
        );

        saveAcademicYear(
                "AY2026",
                "Academic Year 2026-2027",
                2026,
                2027
        );

        List<AcademicYear> result =
                academicYearDao.findAllSorted(
                        "startYear",
                        "asc"
                );

        assertEquals(3, result.size());
        assertEquals(2025, result.get(0).getStartYear());
        assertEquals(2026, result.get(1).getStartYear());
        assertEquals(2027, result.get(2).getStartYear());
    }
}
