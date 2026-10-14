
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface StudentRest {

    @GetMapping
    ResponseEntity<List<StudentResponse>> getAll();

    @GetMapping("/search")
    ResponseEntity<PageResponse<StudentResponse>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @GetMapping("/{id}")
    ResponseEntity<StudentResponse> getById(
            @PathVariable Long id
    );

    @PostMapping
    ResponseEntity<StudentResponse> create(
            @Valid @RequestBody StudentRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<StudentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}
