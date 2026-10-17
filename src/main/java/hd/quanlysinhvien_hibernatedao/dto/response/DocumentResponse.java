
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
        Boolean deleted,
        Boolean active,
        LocalDateTime creationTime,
        LocalDateTime modificationTime
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getAppId(),
                d.getName(),
                d.getPath(),
                d.getOwner(),
                d.getVersion(),
                d.getDeleted(),
                d.getActive(),
                d.getCreationTime(),
                d.getModificationTime()
        );
    }
}
