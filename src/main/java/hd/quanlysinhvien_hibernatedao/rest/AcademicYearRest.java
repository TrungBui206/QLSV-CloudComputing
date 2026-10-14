
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AcademicYearRest {

    @GetMapping("/search")
    ResponseEntity<PageResponse<AcademicYearResponse>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @GetMapping("/{id}")
    ResponseEntity<AcademicYearResponse> getById(
            @PathVariable Long id
    );

    @PostMapping
    ResponseEntity<AcademicYearResponse> create(
            @Valid @RequestBody AcademicYearRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<AcademicYearResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}
