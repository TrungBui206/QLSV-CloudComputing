
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface DepartmentRest {

    @GetMapping
    ResponseEntity<List<DepartmentResponse>> getAll();

    @GetMapping("/search")
    ResponseEntity<PageResponse<DepartmentResponse>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @GetMapping("/{id}")
    ResponseEntity<DepartmentResponse> getById(
            @PathVariable Long id
    );

    @PostMapping
    ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody DepartmentRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}
