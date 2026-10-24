
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClassInfoDaoImplTest {

    @Autowired
    private ClassInfoDaoImpl classInfoDao;

    @Autowired
    private EntityManager entityManager;

    private String saveClassInfo(
            String name,
            String domain,
            String description
    ) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(name);
        classInfo.setDomain(domain);
        classInfo.setDescription(description);

        ClassInfo saved = classInfoDao.create(classInfo);

        entityManager.flush();

        String id = saved.getId();

        entityManager.clear();

        return id;
    }

    @Test
    void create_success() {
        String id = saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Lop khoa hoc may tinh"
        );

        assertNotNull(id);
        assertFalse(id.isBlank());

        ClassInfo found = classInfoDao.get(id);

        assertNotNull(found);
        assertEquals("CSE2026", found.getName());
        assertEquals("Computer Science", found.getDomain());
        assertEquals(
                "Lop khoa hoc may tinh",
                found.getDescription()
        );
    }

    @Test
    void create_autoGenerateUUID() {
        String firstId = saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        String secondId = saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        assertNotNull(firstId);
        assertNotNull(secondId);
        assertNotEquals(firstId, secondId);

        assertEquals(36, firstId.length());
        assertEquals(36, secondId.length());
    }

    @Test
    void get_notFound() {
        ClassInfo result = classInfoDao.get(
                "00000000-0000-0000-0000-000000000000"
        );

        assertNull(result);
    }

    @Test
    void findById_success() {
        String id = saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Test class"
        );

        Optional<ClassInfo> result = classInfoDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals("CSE2026", result.get().getName());
    }

    @Test
    void findByNameIgnoreCase_success() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Test class"
        );

        Optional<ClassInfo> result =
                classInfoDao.findByNameIgnoreCase("cse2026");

        assertTrue(result.isPresent());
        assertEquals("CSE2026", result.get().getName());
    }

    @Test
    void findByNameIgnoreCase_upperCase() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Test class"
        );

        Optional<ClassInfo> result =
                classInfoDao.findByNameIgnoreCase("CSE2026");

        assertTrue(result.isPresent());
    }

    @Test
    void findByNameIgnoreCase_notFound() {
        Optional<ClassInfo> result =
                classInfoDao.findByNameIgnoreCase("UNKNOWN");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByDomainIgnoreCase_success() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Business class"
        );

        List<ClassInfo> result =
                classInfoDao.findByDomainIgnoreCase(
                        "computer science"
                );

        assertEquals(2, result.size());
        assertEquals("CSE2026", result.get(0).getName());
        assertEquals("CSE2027", result.get(1).getName());
    }

    @Test
    void findByDomainIgnoreCase_empty() {
        List<ClassInfo> result =
                classInfoDao.findByDomainIgnoreCase("Unknown");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_byName() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Second class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                0,
                10,
                "name",
                "asc"
        );

        assertEquals(1, result.size());
        assertEquals("CSE2026", result.get(0).getName());
    }

    @Test
    void search_byDomain() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Second class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "computer",
                0,
                10,
                "name",
                "asc"
        );

        assertEquals(1, result.size());
        assertEquals(
                "Computer Science",
                result.get(0).getDomain()
        );
    }

    @Test
    void search_byDescription() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Artificial Intelligence"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Management"
        );

        List<ClassInfo> result = classInfoDao.search(
                "intelligence",
                0,
                10,
                "name",
                "asc"
        );

        assertEquals(1, result.size());
        assertEquals("CSE2026", result.get(0).getName());
    }

    @Test
    void search_caseInsensitiveAndTrim() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Artificial Intelligence"
        );

        List<ClassInfo> result = classInfoDao.search(
                "  COMPUTER  ",
                0,
                10,
                "name",
                "asc"
        );

        assertEquals(1, result.size());
        assertEquals("CSE2026", result.get(0).getName());
    }

    @Test
    void search_nullKeyword() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                null
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                null
        );

        List<ClassInfo> result = classInfoDao.search(
                null,
                0,
                10,
                "name",
                "asc"
        );

        assertEquals(2, result.size());
    }

    @Test
    void search_emptyResult() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Test class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "UNKNOWN999",
                0,
                10,
                "name",
                "asc"
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_pagination() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        saveClassInfo(
                "CSE2028",
                "Computer Science",
                "Third class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                0,
                2,
                "name",
                "asc"
        );

        assertEquals(2, result.size());
        assertEquals("CSE2026", result.get(0).getName());
        assertEquals("CSE2027", result.get(1).getName());
    }

    @Test
    void search_secondPage() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        saveClassInfo(
                "CSE2028",
                "Computer Science",
                "Third class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                2,
                3,
                "name",
                "asc"
        );

        assertEquals(1, result.size());
        assertEquals("CSE2028", result.get(0).getName());
    }

    @Test
    void search_sortAscending() {
        saveClassInfo(
                "CSE2028",
                "Computer Science",
                "Third class"
        );

        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                0,
                10,
                "name",
                "asc"
        );

        assertEquals(3, result.size());
        assertEquals("CSE2026", result.get(0).getName());
        assertEquals("CSE2027", result.get(1).getName());
        assertEquals("CSE2028", result.get(2).getName());
    }

    @Test
    void search_sortDescending() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                0,
                10,
                "name",
                "desc"
        );

        assertEquals(2, result.size());
        assertEquals("CSE2027", result.get(0).getName());
        assertEquals("CSE2026", result.get(1).getName());
    }

    @Test
    void search_sortByDomain() {
        saveClassInfo(
                "CSE2026",
                "Technology",
                "First class"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Second class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "2026",
                0,
                10,
                "domain",
                "asc"
        );

        assertEquals(2, result.size());
        assertEquals("Business", result.get(0).getDomain());
        assertEquals("Technology", result.get(1).getDomain());
    }

    @Test
    void search_invalidSortFieldUsesDefault() {
        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                0,
                10,
                "invalidField",
                "asc"
        );

        assertEquals(2, result.size());
        assertEquals("CSE2026", result.get(0).getName());
        assertEquals("CSE2027", result.get(1).getName());
    }

    @Test
    void search_invalidDirectionUsesAscending() {
        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        List<ClassInfo> result = classInfoDao.search(
                "CSE",
                0,
                10,
                "name",
                "invalidDirection"
        );

        assertEquals(2, result.size());
        assertEquals("CSE2026", result.get(0).getName());
    }

    @Test
    void countSearch_success() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "CSE2027",
                "Computer Science",
                "Second class"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Third class"
        );

        long result = classInfoDao.countSearch("CSE");

        assertEquals(2L, result);
    }

    @Test
    void countSearch_byDescription() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Artificial Intelligence"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Management"
        );

        long result = classInfoDao.countSearch(
                "intelligence"
        );

        assertEquals(1L, result);
    }

    @Test
    void countSearch_nullKeyword() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                null
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                null
        );

        long result = classInfoDao.countSearch(null);

        assertEquals(2L, result);
    }

    @Test
    void update_success() {
        String id = saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Old description"
        );

        ClassInfo existing = classInfoDao.get(id);

        assertNotNull(existing);

        existing.setName("CSE2026A");
        existing.setDomain("Advanced Computer Science");
        existing.setDescription("Updated description");

        classInfoDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        ClassInfo updated = classInfoDao.get(id);

        assertNotNull(updated);
        assertEquals("CSE2026A", updated.getName());
        assertEquals(
                "Advanced Computer Science",
                updated.getDomain()
        );
        assertEquals(
                "Updated description",
                updated.getDescription()
        );
    }

    @Test
    void delete_success() {
        String id = saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Test class"
        );

        assertNotNull(classInfoDao.get(id));

        classInfoDao.deleteByPk(id);

        entityManager.flush();
        entityManager.clear();

        assertNull(classInfoDao.get(id));
    }

    @Test
    void getAll_success() {
        saveClassInfo(
                "CSE2026",
                "Computer Science",
                "First class"
        );

        saveClassInfo(
                "BUS2026",
                "Business",
                "Second class"
        );

        List<ClassInfo> result = classInfoDao.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getAll_empty() {
        List<ClassInfo> result = classInfoDao.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void exists_success() {
        String id = saveClassInfo(
                "CSE2026",
                "Computer Science",
                "Test class"
        );

        assertTrue(classInfoDao.exists(id));
    }

    @Test
    void exists_notFound() {
        assertFalse(
                classInfoDao.exists(
                        "00000000-0000-0000-0000-000000000000"
                )
        );
    }
}
