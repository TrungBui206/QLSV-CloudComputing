
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.rest.DocumentRest;
import hd.quanlysinhvien_hibernatedao.security.authorization.AuthorizationPolicies;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestImpl implements DocumentRest {

    private static final Logger logger =
            LoggerFactory.getLogger(DocumentRestImpl.class);

    private final DocumentService documentService;

    public DocumentRestImpl(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_OR_LECTURER)
    public ResponseEntity<List<DocumentResponse>> create(
            List<MultipartFile> files,
            String appId
    ) {

        try {
            logger.info(
                    "Creating documents: appId={}, totalFiles={}",
                    appId,
                    files == null ? 0 : files.size()
            );

            List<DocumentResponse> result =
                    documentService.create(files, appId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result);

        } catch (RuntimeException e) {
            logger.error("Error creating documents", e);
            throw e;
        }
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<List<DocumentResponse>> listByApp(
            String appId
    ) {

        if (appId == null || appId.isBlank()) {
            return ResponseEntity.ok(
                    documentService.listAll()
            );
        }

        return ResponseEntity.ok(
                documentService.listByApp(appId)
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<List<DocumentResponse>> search(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType,
            String appId
    ) {

        return ResponseEntity.ok(
                documentService.getDoc(
                        query,
                        lowerLimit,
                        upperLimit,
                        orderBy,
                        orderType,
                        appId
                )
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<Long> count(
            String query,
            String appId
    ) {

        return ResponseEntity.ok(
                documentService.getDocCount(query, appId)
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<List<DocumentResponse>> getByApp(
            String appId
    ) {

        return ResponseEntity.ok(
                documentService.listByApp(appId)
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<byte[]> download(
            String filePath
    ) {

        return buildDownloadResponse(filePath);
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<byte[]> downloadById(
            String id
    ) {

        DocumentResponse document =
                documentService.getById(id);

        return buildDownloadResponse(document.path());
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<DocumentResponse> getById(
            String id
    ) {

        return ResponseEntity.ok(
                documentService.getById(id)
        );
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> delete(
            String id
    ) {

        try {
            documentService.delete(id);

            logger.info("Deleted document: id={}", id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            logger.error(
                    "Error deleting document: id={}",
                    id,
                    e
            );
            throw e;
        }
    }

    private ResponseEntity<byte[]> buildDownloadResponse(
            String filePath
    ) {

        byte[] content = documentService.download(filePath);
        String fileName = documentService.getFileName(filePath);

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(
                                fileName,
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
