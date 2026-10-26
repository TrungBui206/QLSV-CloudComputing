package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.rest.StudentRest;
import hd.quanlysinhvien_hibernatedao.security.authorization.AuthorizationPolicies;
import hd.quanlysinhvien_hibernatedao.service.StudentService;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Quản lý sinh viên",
        description = "Các API quản lý thông tin sinh viên"
)
@RestController
@RequestMapping("/api/students")
public class StudentRestImpl implements StudentRest {

    private final StudentService studentService;

    public StudentRestImpl(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAll() {

        List<StudentResponse> students =
                studentService.findAll();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách sinh viên thành công",
                        students
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> search(
            String keyword,
            Long departmentId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        PageResponse<StudentResponse> result =
                studentService.search(
                        keyword,
                        departmentId,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tìm kiếm sinh viên thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<StudentResponse>> getById(
            Long id
    ) {

        StudentResponse student =
                studentService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin sinh viên thành công",
                        student
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_OR_LECTURER)
    public ResponseEntity<ApiResponse<StudentResponse>> create(
            StudentRequest request
    ) {

        StudentResponse student =
                studentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm sinh viên thành công",
                                student
                        )
                );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_OR_LECTURER)
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            Long id,
            StudentRequest request
    ) {

        StudentResponse student =
                studentService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật sinh viên thành công",
                        student
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> delete(Long id) {

        studentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}