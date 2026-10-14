
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;
import hd.quanlysinhvien_hibernatedao.service.StorageService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg"
    );

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final IDocumentDao documentDao;
    private final StorageService storageService;

    public DocumentServiceImpl(
            IDocumentDao documentDao,
            StorageService storageService) {

        this.documentDao = documentDao;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public List<DocumentResponse> create(
            List<MultipartFile> files,
            String appId) {

        if (appId == null || appId.isBlank()
                || appId.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "appId không hợp lệ"
            );
        }

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chưa chọn file"
            );
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }

        List<String> uploadedPaths = new ArrayList<>();
        List<DocumentResponse> result = new ArrayList<>();

        try {
            for (MultipartFile file : files) {

                String originalName = file.getOriginalFilename();

                String objectName = createObjectName(
                        appId, originalName
                );

                try (InputStream input = file.getInputStream()) {
                    storageService.upload(
                            objectName,
                            input,
                            file.getSize(),
                            file.getContentType() == null
                                    ? "application/octet-stream"
                                    : file.getContentType()
                    );
                }

                uploadedPaths.add(objectName);

                Document document = new Document();
                document.setAppId(appId.trim());
                document.setName(originalName);
                document.setPath(objectName);
                document.setOwner("system");

                Document saved = documentDao.create(document);

                result.add(DocumentResponse.from(saved));
            }

            return result;

        } catch (Exception e) {

            for (String path : uploadedPaths) {
                try {
                    storageService.delete(path);
                } catch (Exception cleanupError) {
                    e.addSuppressed(cleanupError);
                }
            }

            if (e instanceof ResponseStatusException responseError) {
                throw responseError;
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể lưu tài liệu",
                    e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listByApp(String appId) {

        if (appId == null || appId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "appId không được để trống"
            );
        }

        return documentDao.findActiveByAppId(appId.trim())
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(String filePath) {

        Document document = findByPath(filePath);

        try (InputStream input =
                     storageService.download(document.getPath())) {

            return input.readAllBytes();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tải tài liệu",
                    e
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getFileName(String filePath) {
        return findByPath(filePath).getName();
    }

    @Override
    @Transactional
    public void delete(String id) {

        Document document = documentDao.findActiveById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài liệu"
                ));

        document.setDeleted(true);
        document.setActive(false);

        documentDao.update(document);
    }

    private Document findByPath(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "filePath không hợp lệ"
            );
        }

        return documentDao.findActiveByPath(filePath)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài liệu"
                ));
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File không được để trống"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File không được quá 10MB"
            );
        }

        String name = file.getOriginalFilename();

        if (name == null || name.isBlank()
                || name.contains("/")
                || name.contains("\\")
                || name.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên file không hợp lệ"
            );
        }

        int dot = name.lastIndexOf('.');

        if (dot < 0 || dot == name.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File không có phần mở rộng hợp lệ"
            );
        }

        String extension = name.substring(dot + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Định dạng file không được hỗ trợ: " + extension
            );
        }
    }

    private String createObjectName(
            String appId,
            String fileName) {

        String safeAppId = appId.trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        String safeName = fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        return safeAppId + "/"
                + UUID.randomUUID()
                + "_"
                + safeName;
    }
}
