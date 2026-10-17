
package hd.quanlysinhvien_hibernatedao.service;

import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    List<DocumentResponse> create(
            List<MultipartFile> files,
            String appId
    );

    List<DocumentResponse> listAll();

    List<DocumentResponse> listByApp(String appId);

    DocumentResponse getById(String id);

    List<DocumentResponse> getDoc(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType,
            String appId
    );

    Long getDocCount(String query, String appId);

    byte[] download(String filePath);

    String getFileName(String filePath);

    void delete(String id);
}
