
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.Document;

import java.util.List;
import java.util.Optional;

public interface IDocumentDao
        extends IGenericDao<String, Document> {

    Document getDocumentName(String fileName, String appId);

    Double getLatestVersion(String fileName, String appId);

    List<Document> getDocumentByAppId(String appId);

    List<Document> findAllNotDeleted();

    Optional<Document> findActiveById(String id);

    Optional<Document> findActiveByPath(String path);

    List<Document> getDoc(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType,
            String appId
    );

    Long getDocCount(String query, String appId);
}
