
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.rest.DocumentRest;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestImpl implements DocumentRest {

    private final DocumentService documentService;

    public DocumentRestImpl(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public ResponseEntity<List<DocumentResponse>> create(
            List<MultipartFile> files,
            String appId) {

        List<DocumentResponse> documents =
                documentService.create(files, appId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documents);
    }

    @Override
    public ResponseEntity<List<DocumentResponse>> listByApp(
            String appId) {

        return ResponseEntity.ok(
                documentService.listByApp(appId)
        );
    }

    @Override
    public ResponseEntity<byte[]> download(String filePath) {

        byte[] content = documentService.download(filePath);

        String fileName = documentService.getFileName(filePath);

        ContentDisposition disposition = ContentDisposition
                .attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @Override
    public ResponseEntity<Void> delete(String id) {

        documentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
