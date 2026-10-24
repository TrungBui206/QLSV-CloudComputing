
package hd.quanlysinhvien_hibernatedao.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

public final class SearchUtils {

    private static final int MAX_PAGE_SIZE = 100;

    private SearchUtils() {
        // Utility class - khong cho phep khoi tao
    }

    // 1. Loai bo khoang trang dau va cuoi
    // Giu nguyen chu hoa, chu thuong
    public static String keyword(String value) {
        return value == null ? "" : value.trim();
    }

    // 2. Chuan hoa chuoi khi tim kiem
    // Chuyen ve chu thuong de so sanh
    public static String normalize(String value) {
        return keyword(value).toLowerCase(Locale.ROOT);
    }

    // 3. Tao Pageable an toan
    public static Pageable pageable(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedSortFields
    ) {

        // Kiem tra so trang va kich thuoc trang
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page phải >= 0 và size trong khoảng 1..100"
            );
        }

        // Chi cho phep sap xep theo cac truong quy dinh
        if (allowedSortFields == null
                || !allowedSortFields.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortBy chỉ nhận: " + allowedSortFields
            );
        }

        // Kiem tra huong sap xep
        if (!"asc".equalsIgnoreCase(direction)
                && !"desc".equalsIgnoreCase(direction)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "direction chỉ nhận asc hoặc desc"
            );
        }

        // Tao Sort theo yeu cau
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Them id ASC de thu tu phan trang on dinh
        if (!"id".equals(sortBy)) {
            sort = sort.and(Sort.by("id").ascending());
        }

        return PageRequest.of(page, size, sort);
    }
}
