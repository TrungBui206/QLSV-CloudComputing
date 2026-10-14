
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
            @RequestParam("appId") String appId
    );

    @GetMapping("/download")
    ResponseEntity<byte[]> download(
            @RequestParam("filePath") String filePath
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable("id") String id
    );
}
