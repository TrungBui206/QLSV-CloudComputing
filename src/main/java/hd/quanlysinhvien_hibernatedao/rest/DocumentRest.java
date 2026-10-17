
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentRest {

    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<List<DocumentResponse>> create(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("appId") String appId
    );

    @GetMapping
    ResponseEntity<List<DocumentResponse>> listByApp(
            @RequestParam(value = "appId", required = false)
            String appId
    );

    @GetMapping("/search")
    ResponseEntity<List<DocumentResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") Integer lowerLimit,
            @RequestParam(defaultValue = "10") Integer upperLimit,
            @RequestParam(defaultValue = "creationTime") String orderBy,
            @RequestParam(defaultValue = "desc") String orderType,
            @RequestParam(required = false) String appId
    );

    @GetMapping("/count")
    ResponseEntity<Long> count(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String appId
    );

    @GetMapping("/app/{appId}")
    ResponseEntity<List<DocumentResponse>> getByApp(
            @PathVariable("appId") String appId
    );

    @GetMapping("/download")
    ResponseEntity<byte[]> download(
            @RequestParam("filePath") String filePath
    );

    @GetMapping("/download/{id}")
    ResponseEntity<byte[]> downloadById(
            @PathVariable("id") String id
    );

    @GetMapping("/{id}")
    ResponseEntity<DocumentResponse> getById(
            @PathVariable("id") String id
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable("id") String id
    );
}
