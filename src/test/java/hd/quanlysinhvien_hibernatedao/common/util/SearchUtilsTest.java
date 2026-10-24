
package hd.quanlysinhvien_hibernatedao.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchUtilsTest {

    private static final Set<String> ALLOWED_FIELDS =
            Set.of("id", "name", "createdAt");

    @Test
    void keyword_validValue() {
        assertEquals(
                "Nguyen Van A",
                SearchUtils.keyword("  Nguyen Van A  ")
        );
    }

    @Test
    void keyword_nullValue() {
        assertEquals("", SearchUtils.keyword(null));
    }

    @Test
    void keyword_blankValue() {
        assertEquals("", SearchUtils.keyword("   "));
    }

    @Test
    void normalize_validValue() {
        assertEquals(
                "nguyen van a",
                SearchUtils.normalize("  NGUYEN VAN A  ")
        );
    }

    @Test
    void normalize_nullValue() {
        assertEquals("", SearchUtils.normalize(null));
    }

    @Test
    void pageable_ascending() {
        Pageable pageable = SearchUtils.pageable(
                0, 10, "name", "asc", ALLOWED_FIELDS
        );

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("name").getDirection()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void pageable_descending() {
        Pageable pageable = SearchUtils.pageable(
                1, 20, "name", "desc", ALLOWED_FIELDS
        );

        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("name").getDirection()
        );
    }

    @Test
    void pageable_sortById() {
        Pageable pageable = SearchUtils.pageable(
                0, 10, "id", "desc", ALLOWED_FIELDS
        );

        assertEquals(1, pageable.getSort().toList().size());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().getOrderFor("id").getDirection()
        );
    }

    @Test
    void pageable_maxPageSize() {
        Pageable pageable = SearchUtils.pageable(
                0, 100, "id", "asc", ALLOWED_FIELDS
        );

        assertEquals(100, pageable.getPageSize());
    }

    @Test
    void pageable_negativePage_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        -1, 10, "name", "asc", ALLOWED_FIELDS
                )
        );
    }

    @Test
    void pageable_zeroSize_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        0, 0, "name", "asc", ALLOWED_FIELDS
                )
        );
    }

    @Test
    void pageable_sizeOverLimit_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        0, 101, "name", "asc", ALLOWED_FIELDS
                )
        );
    }

    @Test
    void pageable_invalidSortField_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        0, 10, "password", "asc", ALLOWED_FIELDS
                )
        );
    }

    @Test
    void pageable_nullAllowedFields_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        0, 10, "name", "asc", null
                )
        );
    }

    @Test
    void pageable_invalidDirection_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        0, 10, "name", "random", ALLOWED_FIELDS
                )
        );
    }

    @Test
    void pageable_nullDirection_400() {
        assertBadRequest(() ->
                SearchUtils.pageable(
                        0, 10, "name", null, ALLOWED_FIELDS
                )
        );
    }

    private void assertBadRequest(
            org.junit.jupiter.api.function.Executable action
    ) {
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        action
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }
}
