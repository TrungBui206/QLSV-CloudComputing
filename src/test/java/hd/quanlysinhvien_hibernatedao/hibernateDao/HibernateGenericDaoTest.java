
package hd.quanlysinhvien_hibernatedao.hibernateDao;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HibernateGenericDaoTest {

    private static SessionFactory sessionFactory;

    private EntityManager entityManager;

    private HibernateGenericDao<Long, TestItem> dao;

    @BeforeAll
    static void startDatabase() {

        sessionFactory = new Configuration()
                .addAnnotatedClass(TestItem.class)
                .setProperty(
                        "hibernate.connection.driver_class",
                        "org.h2.Driver"
                )
                .setProperty(
                        "hibernate.connection.url",
                        "jdbc:h2:mem:generic_dao_test;DB_CLOSE_DELAY=-1"
                )
                .setProperty(
                        "hibernate.connection.username",
                        "sa"
                )
                .setProperty(
                        "hibernate.connection.password",
                        ""
                )
                .setProperty(
                        "hibernate.hbm2ddl.auto",
                        "create-drop"
                )
                .setProperty(
                        "hibernate.show_sql",
                        "false"
                )
                .buildSessionFactory();
    }

    @BeforeEach
    void setUp() {

        entityManager = sessionFactory.createEntityManager();

        entityManager.getTransaction().begin();

        dao = new HibernateGenericDao<>(
                TestItem.class,
                entityManager
        );
    }

    @AfterEach
    void tearDown() {

        try {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        } finally {
            entityManager.close();
        }
    }

    @AfterAll
    static void stopDatabase() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private TestItem persist(
            String name,
            String groupCode
    ) {

        TestItem item = dao.create(
                new TestItem(name, groupCode)
        );

        dao.flush();

        return item;
    }

    // ==========================================
    // TEST 1: ENTITY METADATA
    // ==========================================

    @Test
    void metadataIsAvailable() {

        assertSame(
                TestItem.class,
                dao.getType()
        );

        assertSame(
                entityManager,
                dao.getEntityManager()
        );
    }

    // ==========================================
    // TEST 2: CREATE AND FIND BY ID
    // ==========================================

    @Test
    void createAndGetById() {

        TestItem saved = persist("An", "A");

        assertNotNull(saved.getId());

        assertEquals(
                "An",
                dao.get(saved.getId()).getName()
        );

        assertEquals(
                "An",
                dao.findById(saved.getId())
                        .orElseThrow()
                        .getName()
        );
    }

    // ==========================================
    // TEST 3: NULL VALIDATION
    // ==========================================

    @Test
    void createUpdateAndSaveOrUpdateRejectNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.create(null)
        );

        // Ep kieu de tranh trung voi
        // update(UpdateSpecification)
        assertThrows(
                IllegalArgumentException.class,
                () -> dao.update((TestItem) null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.saveOrUpdate(null)
        );
    }

    // ==========================================
    // TEST 4: UPDATE AND SAVE OR UPDATE
    // ==========================================

    @Test
    void updateAndSaveOrUpdateWork() {

        TestItem saved = persist("An", "A");

        saved.setName("Binh");

        assertEquals(
                "Binh",
                dao.update(saved).getName()
        );

        TestItem inserted = dao.saveOrUpdate(
                new TestItem("Chi", "B")
        );

        dao.flush();

        assertNotNull(inserted.getId());

        assertEquals(
                2L,
                dao.count()
        );
    }

    // ==========================================
    // TEST 5: NULL AND MISSING ID
    // ==========================================

    @Test
    void getAndFindByIdHandleNullAndMissingIds() {

        assertNull(
                dao.get(null)
        );

        assertTrue(
                dao.findById(null).isEmpty()
        );

        assertNull(
                dao.get(99999L)
        );

        assertTrue(
                dao.findById(99999L).isEmpty()
        );
    }

    // ==========================================
    // TEST 6: GET BY FIELD
    // ==========================================

    @Test
    void getByFieldFindsFirstMatchOrReturnsNull() {

        persist("An", "A");

        assertEquals(
                "An",
                dao.getByField("name", "An").getName()
        );

        assertNull(
                dao.getByField("name", "Not found")
        );

        assertNull(
                dao.getByField("invalid-field", "An")
        );

        assertNull(
                dao.getByField(null, "An")
        );
    }

    // ==========================================
    // TEST 7: FIND BY FIELD
    // ==========================================

    @Test
    void findByFieldFiltersAndRejectsInvalidIdentifier() {

        persist("An", "A");
        persist("Binh", "A");
        persist("Chi", "B");

        assertEquals(
                2,
                dao.findByField("groupCode", "A").size()
        );

        assertTrue(
                dao.findByField("groupCode", "Missing")
                        .isEmpty()
        );

        assertTrue(
                dao.findByField("name!", "An")
                        .isEmpty()
        );

        assertTrue(
                dao.findByField(null, "An")
                        .isEmpty()
        );
    }

    // ==========================================
    // TEST 8: GET ALL, COUNT, EXISTS
    // ==========================================

    @Test
    void getAllCountAndExistsWork() {

        TestItem saved = persist("An", "A");

        assertEquals(
                1,
                dao.getAll().size()
        );

        assertEquals(
                1L,
                dao.count()
        );

        assertTrue(
                dao.exists(saved.getId())
        );

        assertFalse(
                dao.exists(99999L)
        );

        // Ep kieu Long de goi exists(Pk)
        // thay vi exists(Example)
        assertFalse(
                dao.exists((Long) null)
        );
    }

    // ==========================================
    // TEST 9: BULK CREATE AND DELETE
    // ==========================================

    @Test
    void createAllAndDeleteAllWork() {

        List<TestItem> inserted = dao.createAll(
                List.of(
                        new TestItem("An", "A"),
                        new TestItem("Binh", "B")
                )
        );

        dao.flush();

        assertEquals(
                2,
                inserted.size()
        );

        assertEquals(
                2L,
                dao.count()
        );

        dao.deleteAll(inserted);

        dao.flush();

        assertEquals(
                0L,
                dao.count()
        );
    }

    // ==========================================
    // TEST 10: EMPTY BULK OPERATIONS
    // ==========================================

    @Test
    void bulkOperationsHandleNullAndEmptyList() {

        assertTrue(
                dao.createAll(null).isEmpty()
        );

        assertTrue(
                dao.createAll(List.of()).isEmpty()
        );

        assertDoesNotThrow(
                () -> dao.deleteAll(null)
        );

        assertDoesNotThrow(
                () -> dao.deleteAll(List.of())
        );
    }

    // ==========================================
    // TEST 11: DELETE OPERATIONS
    // ==========================================

    @Test
    void deleteByPkDeleteByIdAndDeleteEntityWork() {

        TestItem one = persist("An", "A");
        TestItem two = persist("Binh", "A");
        TestItem three = persist("Chi", "B");

        dao.deleteByPk(one.getId());

        dao.deleteById(two.getId());

        dao.delete(three);

        dao.flush();

        assertEquals(
                0L,
                dao.count()
        );
    }

    // ==========================================
    // TEST 12: DELETE NULL VALIDATION
    // ==========================================

    @Test
    void deleteOperationsRejectNull() {

        // Ep kieu de tranh trung voi
        // delete(DeleteSpecification)
        assertThrows(
                IllegalArgumentException.class,
                () -> dao.delete((TestItem) null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.deleteById(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.deleteByPk(null)
        );
    }

    // ==========================================
    // TEST 13: FLUSH AND CLEAR
    // ==========================================

    @Test
    void flushAndClearAreFunctional() {

        TestItem saved = persist("An", "A");

        assertTrue(
                entityManager.contains(saved)
        );

        dao.clear();

        assertFalse(
                entityManager.contains(saved)
        );

        assertEquals(
                "An",
                dao.get(saved.getId()).getName()
        );
    }

    // ==========================================
    // TEST 14: IDENTIFIER VALIDATION
    // ==========================================

    @Test
    void identifierValidationMatchesCurrentContract() {

        assertTrue(
                dao.isValidIdentifier("groupCode")
        );

        assertTrue(
                dao.isValidIdentifier("name_1")
        );

        assertFalse(
                dao.isValidIdentifier(null)
        );

        assertFalse(
                dao.isValidIdentifier("")
        );

        assertFalse(
                dao.isValidIdentifier("name.bad")
        );

        assertFalse(
                dao.isValidIdentifier("name;DELETE")
        );
    }

    // ==========================================
    // TEST ENTITY - CHI DUNG CHO H2
    // ==========================================

    @Entity(name = "GenericDaoTestItem")
    @Table(name = "generic_dao_test_item")
    public static class TestItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private String groupCode;

        public TestItem() {
        }

        public TestItem(
                String name,
                String groupCode
        ) {
            this.name = name;
            this.groupCode = groupCode;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGroupCode() {
            return groupCode;
        }
    }
}
