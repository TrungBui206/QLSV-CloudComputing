package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.rest.ClassInfoRest;
import hd.quanlysinhvien_hibernatedao.service.ClassInfoService;
import hd.quanlysinhvien_hibernatedao.security.authorization.AuthorizationPolicies;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Quản lý lớp học",
        description = "Các API quản lý thông tin lớp học"
)
@RestController
@RequestMapping("/api/classes")
public class ClassInfoRestImpl implements ClassInfoRest {

    private static final Logger logger =
            LoggerFactory.getLogger(ClassInfoRestImpl.class);

    private final ClassInfoService classInfoService;

    public ClassInfoRestImpl(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_OR_LECTURER)
    public ResponseEntity<ApiResponse<ClassInfoResponse>> createClassInfo(
            ClassInfoRequest request
    ) {
        logger.info("Creating ClassInfo");

        ClassInfoResponse result =
                classInfoService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Thêm lớp học thành công",
                                result
                        )
                );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_OR_LECTURER)
    public ResponseEntity<ApiResponse<ClassInfoResponse>> updateClassInfo(
            String id,
            ClassInfoRequest request
    ) {
        logger.info("Updating ClassInfo: id={}", id);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu lớp học không hợp lệ"
            );
        }

        if (request.id() != null && !request.id().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID trong URL và Body không khớp"
            );
        }

        ClassInfoRequest updateRequest = new ClassInfoRequest(
                id,
                request.name(),
                request.domain(),
                request.description()
        );

        ClassInfoResponse result =
                classInfoService.update(updateRequest);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật lớp học thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<Void>> deleteClassInfo(
            String id
    ) {
        logger.info("Deleting ClassInfo: id={}", id);

        classInfoService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa lớp học thành công",
                        null
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<ClassInfoResponse>> getClassInfoById(
            String id
    ) {
        ClassInfoResponse result =
                classInfoService.getById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin lớp học thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<ClassInfoResponse>> getClassInfoByName(
            String name
    ) {
        ClassInfoResponse result =
                classInfoService.getByName(name);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tìm lớp học theo tên thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<List<ClassInfoResponse>>> getAllClassInfo() {

        List<ClassInfoResponse> result =
                classInfoService.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách lớp học thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<List<ClassInfoResponse>>> getClassInfoByDomain(
            String domain
    ) {
        List<ClassInfoResponse> result =
                classInfoService.getByDomain(domain);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy danh sách lớp học theo lĩnh vực thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<ApiResponse<List<ClassInfoResponse>>> searchClassInfo(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType
    ) {
        List<ClassInfoResponse> result =
                classInfoService.search(
                        query,
                        lowerLimit,
                        upperLimit,
                        orderBy,
                        orderType
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tìm kiếm lớp học thành công",
                        result
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<ApiResponse<Integer>> getClassInfoCount(
            String query
    ) {
        Integer result = classInfoService.getCount(query);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đếm số lớp học thành công",
                        result
                )
        );
    }
}