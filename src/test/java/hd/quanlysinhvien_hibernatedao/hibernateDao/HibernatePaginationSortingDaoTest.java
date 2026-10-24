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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HibernatePaginationSortingDaoTest {

    private static SessionFactory sessionFactory;

    private EntityManager entityManager;
    private HibernatePaginationSortingDao<Long, TestItem> dao;

    @BeforeAll
    static void startDatabase() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(TestItem.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:pagination_dao_test;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.show_sql", "false")
                .buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        entityManager = sessionFactory.createEntityManager();
        entityManager.getTransaction().begin();
        dao = new HibernatePaginationSortingDao<>(TestItem.class, entityManager);

        dao.createAll(List.of(
                new TestItem("An", "A"),
                new TestItem("Binh", "B"),
                new TestItem("Chi", "A"),
                new TestItem("Dung", "A")
        ));
        dao.flush();
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
        sessionFactory.close();
    }

    private Specification<TestItem> inGroupA() {
        return (root, query, builder) ->
                builder.equal(root.get("groupCode"), "A");
    }

    @Test
    void findAllSortedRespectsAscendingAndDescending() {
        List<TestItem> ascending = dao.findAllSorted("name", "asc");
        List<TestItem> descending = dao.findAllSorted("name", "desc");

        assertEquals("An", ascending.get(0).getName());
        assertEquals("Dung", descending.get(0).getName());
        assertEquals(4, ascending.size());
    }

    @Test
    void findAllPagedReturnsCorrectPagesAndTotal() {
        Page<TestItem> first = dao.findAllPaged(0, 2, "name", "asc");
        Page<TestItem> second = dao.findAllPaged(1, 2, "name", "asc");

        assertEquals(4L, first.getTotalElements());
        assertEquals(2, first.getTotalPages());
        assertEquals(List.of("An", "Binh"),
                first.getContent().stream().map(TestItem::getName).toList());
        assertEquals(List.of("Chi", "Dung"),
                second.getContent().stream().map(TestItem::getName).toList());
    }

    @Test
    void findByFieldSortedWithSortFiltersAndOrders() {
        List<TestItem> result = dao.findByFieldSorted(
                "groupCode", "A", Sort.by(Sort.Direction.DESC, "name")
        );
        assertEquals(List.of("Dung", "Chi", "An"),
                result.stream().map(TestItem::getName).toList());
    }

    @Test
    void findByFieldSortedWithStringOverloadWorks() {
        List<TestItem> result = dao.findByFieldSorted(
                "groupCode", "A", "name", "asc"
        );
        assertEquals(List.of("An", "Chi", "Dung"),
                result.stream().map(TestItem::getName).toList());
    }

    @Test
    void findByFieldSortedWithNullSortUsesUnsorted() {
        List<TestItem> result = dao.findByFieldSorted(
                "groupCode", "A", (Sort) null
        );
        assertEquals(3, result.size());
    }

    @Test
    void findByFieldSortedWithInvalidIdentifierReturnsEmpty() {
        assertTrue(dao.findByFieldSorted(
                "invalid-field", "A", Sort.by("name")
        ).isEmpty());
    }

    @Test
    void findByFieldSortedWithNullValueMatchesNothing() {
        assertTrue(dao.findByFieldSorted(
                "groupCode", null, Sort.by("name")
        ).isEmpty());
    }

    @Test
    void findByFieldPagedWithPageableFilters() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));
        Page<TestItem> page = dao.findByFieldPaged("groupCode", "A", pageable);

        assertEquals(3L, page.getTotalElements());
        assertEquals(List.of("An", "Chi"),
                page.getContent().stream().map(TestItem::getName).toList());
    }

    @Test
    void findByFieldPagedWithStringOverloadWorks() {
        Page<TestItem> page = dao.findByFieldPaged(
                "groupCode", "A", 0, 2, "name", "desc"
        );
        assertEquals(3L, page.getTotalElements());
        assertEquals(List.of("Dung", "Chi"),
                page.getContent().stream().map(TestItem::getName).toList());
    }

    @Test
    void findByFieldPagedWithInvalidIdentifierReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<TestItem> page = dao.findByFieldPaged("invalid-field", "A", pageable);
        assertTrue(page.isEmpty());
        assertEquals(0L, page.getTotalElements());
    }

    @Test
    void findByFieldPagedWithNullValueMatchesNothing() {
        Page<TestItem> page = dao.findByFieldPaged(
                "groupCode", null, PageRequest.of(0, 2)
        );
        assertTrue(page.isEmpty());
    }

    @Test
    void specificationOverloadsWork() {
        Specification<TestItem> specification = inGroupA();

        List<TestItem> plain = dao.findAll(specification);
        List<TestItem> sorted = dao.findAll(specification, Sort.by("name").descending());
        Page<TestItem> paged = dao.findAll(
                specification, PageRequest.of(0, 2, Sort.by("name"))
        );

        assertEquals(3, plain.size());
        assertEquals("Dung", sorted.get(0).getName());
        assertEquals(3L, paged.getTotalElements());
        assertEquals(2, paged.getContent().size());
    }

    @Test
    void getAllOverloadsWork() {
        List<TestItem> sorted = dao.getAll(Sort.by("name").descending());
        Page<TestItem> paged = dao.getAll(PageRequest.of(0, 2));

        assertEquals("Dung", sorted.get(0).getName());
        assertEquals(2, paged.getContent().size());
        assertEquals(4L, paged.getTotalElements());
    }

    @Test
    void buildSortHandlesValidAndInvalidInput() {
        assertTrue(dao.buildSort(null, "asc").isUnsorted());
        assertTrue(dao.buildSort("", "asc").isUnsorted());
        assertTrue(dao.buildSort("bad-field", "asc").isUnsorted());

        assertEquals(Sort.Direction.DESC,
                dao.buildSort("name", "DESC").getOrderFor("name").getDirection());
        assertEquals(Sort.Direction.ASC,
                dao.buildSort("name", "other").getOrderFor("name").getDirection());
    }

    @Entity(name = "PaginationDaoTestItem")
    @Table(name = "pagination_dao_test_item")
    public static class TestItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String groupCode;

        public TestItem() {
        }

        public TestItem(String name, String groupCode) {
            this.name = name;
            this.groupCode = groupCode;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getGroupCode() {
            return groupCode;
        }
    }
}
