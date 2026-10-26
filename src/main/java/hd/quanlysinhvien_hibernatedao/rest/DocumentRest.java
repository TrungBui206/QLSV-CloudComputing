
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentRest {

    @Operation(
            summary = "Tải tài liệu lên",
            description = "Tải một hoặc nhiều tài liệu lên hệ thống MinIO"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Tải tài liệu lên thành công",
            useReturnTypeSchema = true
    )
    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<ApiResponse<List<DocumentResponse>>> create(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("appId") String appId
    );

    @Operation(
            summary = "Lấy danh sách tài liệu",
            description = "Lấy toàn bộ tài liệu hoặc lọc theo mã ứng dụng"
    )
    @GetMapping
    ResponseEntity<ApiResponse<List<DocumentResponse>>> listByApp(
            @RequestParam(value = "appId", required = false)
            String appId
    );

    @Operation(
            summary = "Tìm kiếm tài liệu",
            description = "Tìm kiếm, sắp xếp và giới hạn số lượng tài liệu trả về"
    )
    @GetMapping("/search")
    ResponseEntity<ApiResponse<List<DocumentResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") Integer lowerLimit,
            @RequestParam(defaultValue = "10") Integer upperLimit,
            @RequestParam(defaultValue = "creationTime") String orderBy,
            @RequestParam(defaultValue = "desc") String orderType,
            @RequestParam(required = false) String appId
    );

    @Operation(
            summary = "Đếm số tài liệu",
            description = "Đếm số tài liệu theo từ khóa hoặc mã ứng dụng"
    )
    @GetMapping("/count")
    ResponseEntity<ApiResponse<Long>> count(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String appId
    );

    @Operation(
            summary = "Lấy tài liệu theo ứng dụng",
            description = "Lấy danh sách tài liệu thuộc một ứng dụng cụ thể"
    )
    @GetMapping("/app/{appId}")
    ResponseEntity<ApiResponse<List<DocumentResponse>>> getByApp(
            @PathVariable("appId") String appId
    );

    @Operation(
            summary = "Tải tài liệu theo đường dẫn",
            description = "Tải file từ MinIO bằng đường dẫn lưu trữ"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Trả về nội dung file",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(
                            type = "string",
                            format = "binary"
                    )
            )
    )
    @GetMapping("/download")
    ResponseEntity<byte[]> download(
            @RequestParam("filePath") String filePath
    );

    @Operation(
            summary = "Tải tài liệu theo ID",
            description = "Tìm tài liệu theo ID và trả về nội dung file từ MinIO"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Trả về nội dung file",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(
                            type = "string",
                            format = "binary"
                    )
            )
    )
    @GetMapping("/download/{id}")
    ResponseEntity<byte[]> downloadById(
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Lấy thông tin tài liệu theo ID",
            description = "Trả về thông tin tài liệu, không tải nội dung file"
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<DocumentResponse>> getById(
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Xóa tài liệu",
            description = "Xóa tài liệu khỏi hệ thống theo ID"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Xóa tài liệu thành công, không trả về nội dung",
            content = @Content
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable("id") String id
    );
}
