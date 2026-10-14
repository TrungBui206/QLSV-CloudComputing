
package hd.quanlysinhvien_hibernatedao.dao;

import hd.quanlysinhvien_hibernatedao.entity.Document;

import java.util.List;
import java.util.Optional;

public interface IDocumentDao extends IGenericDao<String, Document> {

    Optional<Document> findActiveById(String id);

    Optional<Document> findActiveByPath(String path);

    List<Document> findActiveByAppId(String appId);
}
