
package hd.quanlysinhvien_hibernatedao.dto.response;

import hd.quanlysinhvien_hibernatedao.entity.Document;

import java.time.LocalDateTime;

public record DocumentResponse(
        String id,
        String appId,
        String name,
        String path,
        String owner,
        Double version,
        LocalDateTime creationTime,
        LocalDateTime modificationTime
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getAppId(),
                document.getName(),
                document.getPath(),
                document.getOwner(),
                document.getVersion(),
                document.getCreationTime(),
                document.getModificationTime()
        );
    }
}
