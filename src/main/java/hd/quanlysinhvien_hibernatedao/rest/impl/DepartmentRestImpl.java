package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.rest.DepartmentRest;
import hd.quanlysinhvien_hibernatedao.security.authorization.AuthorizationPolicies;
import hd.quanlysinhvien_hibernatedao.service.DepartmentService;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Quản lý khoa",
        description = "Các API quản lý thông tin khoa"
)

@RestController
@RequestMapping("/api/departments")
public class DepartmentRestImpl implements DepartmentRest {

    private final DepartmentService departmentService;

    public DepartmentRestImpl(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll() {

        List<DepartmentResponse> departments =
                departmentService.findAll();

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khoa thành công", departments));
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        PageResponse<DepartmentResponse> result =
                departmentService.search(
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khoa thành công", result));
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(Long id) {

        DepartmentResponse department =
                departmentService.findById(id);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khoa thành công", department));
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            DepartmentRequest request
    ) {

        DepartmentResponse department =
                departmentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm khoa thành công", department));
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            Long id,
            DepartmentRequest request
    ) {

        DepartmentResponse department =
                departmentService.update(id, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật khoa thành công", department));
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> delete(Long id) {

        departmentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
