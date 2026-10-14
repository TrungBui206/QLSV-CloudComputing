
package hd.quanlysinhvien_hibernatedao.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

public final class SearchUtils {

    private SearchUtils() {
    }

    public static Pageable pageable(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedSortFields) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page phải >= 0 và size trong khoảng 1..100"
            );
        }

        if (!allowedSortFields.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortBy chỉ nhận: " + allowedSortFields
            );
        }

        if (!"asc".equalsIgnoreCase(direction)
                && !"desc".equalsIgnoreCase(direction)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "direction chỉ nhận asc hoặc desc"
            );
        }

        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        if (!"id".equals(sortBy)) {
            sort = sort.and(Sort.by("id"));
        }

        return PageRequest.of(page, size, sort);
    }

    public static String keyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
