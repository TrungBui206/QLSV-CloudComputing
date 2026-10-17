
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;
import hd.quanlysinhvien_hibernatedao.service.StorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger =
            LoggerFactory.getLogger(DocumentServiceImpl.class);

    private static final Set<String> EXTENSIONS = Set.of(
            "pdf", "xlsx", "xls",
            "doc", "docx", "jpg", "jpeg"
    );

    private static final long MAX_FILE_SIZE =
            10L * 1024 * 1024;

    private static final long MAX_REQUEST_SIZE =
            50L * 1024 * 1024;

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

        if (appId == null
                || appId.isBlank()
                || appId.length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã hồ sơ không hợp lệ"
            );
        }

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chưa chọn tài liệu"
            );
        }

        String normalizedAppId = appId.trim();

        long totalSize = 0;

        for (MultipartFile file : files) {
            validateFile(file);

            totalSize += file.getSize();

            if (totalSize > MAX_REQUEST_SIZE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tổng dung lượng vượt quá 50MB"
                );
            }
        }

        List<String> uploadedPaths = new ArrayList<>();
        List<DocumentResponse> result = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();

                String filePath = createFilePath(
                        normalizedAppId,
                        fileName
                );

                try (InputStream input = file.getInputStream()) {
                    storageService.upload(
                            filePath,
                            input,
                            file.getSize(),
                            file.getContentType() == null
                                    ? "application/octet-stream"
                                    : file.getContentType()
                    );
                }

                uploadedPaths.add(filePath);

                Document oldDocument =
                        documentDao.getDocumentName(
                                fileName,
                                normalizedAppId
                        );

                Double lastVersion =
                        documentDao.getLatestVersion(
                                fileName,
                                normalizedAppId
                        );

                if (oldDocument != null) {
                    oldDocument.setActive(false);
                    documentDao.update(oldDocument);
                }

                Document document = new Document();

                document.setAppId(normalizedAppId);
                document.setName(fileName);
                document.setPath(filePath);
                document.setOwner("system");
                document.setDeleted(false);
                document.setActive(true);

                document.setVersion(
                        lastVersion == null
                                ? 1.0
                                : lastVersion + 1.0
                );

                Document saved = documentDao.create(document);

                documentDao.flush();

                result.add(DocumentResponse.from(saved));

                logger.info(
                        "Created document: name={}, appId={}, version={}",
                        saved.getName(),
                        saved.getAppId(),
                        saved.getVersion()
                );
            }

            return result;

        } catch (Exception e) {
            logger.error("Error creating documents", e);

            for (String path : uploadedPaths) {
                try {
                    storageService.delete(path);
                } catch (Exception cleanupError) {
                    logger.error(
                            "Error cleaning up MinIO object: {}",
                            path,
                            cleanupError
                    );
                    e.addSuppressed(cleanupError);
                }
            }

            if (e instanceof ResponseStatusException error) {
                throw error;
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
    public List<DocumentResponse> listAll() {

        return documentDao.findAllNotDeleted()
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listByApp(String appId) {

        if (appId == null || appId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mã hồ sơ không hợp lệ"
            );
        }

        return documentDao.getDocumentByAppId(
                        appId.trim()
                )
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getById(String id) {

        return DocumentResponse.from(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDoc(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType,
            String appId) {

        return documentDao.getDoc(
                        query,
                        lowerLimit,
                        upperLimit,
                        orderBy,
                        orderType,
                        appId
                )
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDocCount(
            String query,
            String appId) {

        return documentDao.getDocCount(query, appId);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] download(String filePath) {

        Document document = findByPath(filePath);

        try (InputStream input =
                     storageService.download(document.getPath())) {

            return input.readAllBytes();

        } catch (Exception e) {
            logger.error(
                    "Error downloading document: {}",
                    filePath,
                    e
            );

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

        Document document = findById(id);

        document.setDeleted(true);
        document.setActive(false);

        documentDao.update(document);

        logger.info(
                "Soft deleted document: id={}, name={}",
                document.getId(),
                document.getName()
        );
    }

    private Document findById(String id) {

        if (id == null || id.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID tài liệu không hợp lệ"
            );
        }

        return documentDao.findActiveById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tài liệu"
                ));
    }

    private Document findByPath(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Đường dẫn tài liệu không hợp lệ"
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

        String fileName = file.getOriginalFilename();

        if (fileName == null
                || fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.contains("..")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên file không hợp lệ"
            );
        }

        int dot = fileName.lastIndexOf('.');

        if (dot <= 0 || dot == fileName.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên file không có phần mở rộng hợp lệ"
            );
        }

        String baseName = fileName.substring(0, dot);

        if (baseName.length() > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tên tài liệu không được quá 50 ký tự"
            );
        }

        String extension = fileName.substring(dot + 1)
                .toLowerCase(Locale.ROOT);

        if (!EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Định dạng file không được hỗ trợ"
            );
        }
    }

    private String createFilePath(
            String appId,
            String fileName) {

        String safeAppId = appId.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );

        String safeName = fileName.replaceAll(
                "[^\\p{L}\\p{N}._ -]",
                "_"
        );

        return safeAppId
                + "/"
                + UUID.randomUUID()
                + "_"
                + safeName;
    }
}
