package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ClassInfoRest {

    @Operation(
            summary = "Thêm lớp học",
            description = "Chỉ ADMIN và LECTURER được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Thêm lớp học thành công",
            useReturnTypeSchema = true
    )
    @PostMapping
    ResponseEntity<ApiResponse<ClassInfoResponse>> createClassInfo(
            @RequestBody ClassInfoRequest request
    );

    @Operation(
            summary = "Cập nhật lớp học",
            description = "Chỉ ADMIN và LECTURER được phép thực hiện"
    )
    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<ClassInfoResponse>> updateClassInfo(
            @PathVariable("id") String id,
            @RequestBody ClassInfoRequest request
    );

    @Operation(
            summary = "Xóa lớp học",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Xóa lớp học thành công",
            useReturnTypeSchema = true
    )
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> deleteClassInfo(
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Lấy lớp học theo ID",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<ClassInfoResponse>> getClassInfoById(
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Lấy lớp học theo tên",
            description = "Tìm thông tin lớp học bằng tên lớp"
    )
    @GetMapping("/name/{name}")
    ResponseEntity<ApiResponse<ClassInfoResponse>> getClassInfoByName(
            @PathVariable("name") String name
    );

    @Operation(
            summary = "Lấy danh sách lớp học",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping
    ResponseEntity<ApiResponse<List<ClassInfoResponse>>> getAllClassInfo();

    @Operation(
            summary = "Lấy lớp học theo ngành",
            description = "Lọc danh sách lớp học theo ngành"
    )
    @GetMapping("/domain/{domain}")
    ResponseEntity<ApiResponse<List<ClassInfoResponse>>> getClassInfoByDomain(
            @PathVariable("domain") String domain
    );

    @Operation(
            summary = "Tìm kiếm lớp học",
            description = "Tìm kiếm, sắp xếp và giới hạn kết quả lớp học"
    )
    @GetMapping("/search")
    ResponseEntity<ApiResponse<List<ClassInfoResponse>>> searchClassInfo(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "lowerLimit", required = false) Integer lowerLimit,
            @RequestParam(value = "upperLimit", required = false) Integer upperLimit,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "orderType", required = false) String orderType
    );

    @Operation(
            summary = "Đếm số lớp học",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @GetMapping("/count")
    ResponseEntity<ApiResponse<Integer>> getClassInfoCount(
            @RequestParam(value = "query", required = false) String query
    );
}