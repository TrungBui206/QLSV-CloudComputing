
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.rest.DepartmentRest;
import hd.quanlysinhvien_hibernatedao.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentRestImpl implements DepartmentRest {

    private final DepartmentService departmentService;

    public DepartmentRestImpl(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<List<DepartmentResponse>> getAll() {

        List<DepartmentResponse> departments =
                departmentService.findAll();

        return ResponseEntity.ok(departments);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<PageResponse<DepartmentResponse>> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        PageResponse<DepartmentResponse> result =
                departmentService.search(
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<DepartmentResponse> getById(Long id) {

        DepartmentResponse department =
                departmentService.findById(id);

        return ResponseEntity.ok(department);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> create(
            DepartmentRequest request) {

        DepartmentResponse department =
                departmentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(department);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> update(
            Long id,
            DepartmentRequest request) {

        DepartmentResponse department =
                departmentService.update(id, request);

        return ResponseEntity.ok(department);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(Long id) {

        departmentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
