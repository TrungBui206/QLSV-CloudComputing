
package hd.quanlysinhvien_hibernatedao.dto.response;

import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

public record ClassInfoResponse(
        String id,
        String name,
        String domain,
        String description
) {

    public static ClassInfoResponse from(ClassInfo entity) {
        return new ClassInfoResponse(
                entity.getId(),
                entity.getName(),
                entity.getDomain(),
                entity.getDescription()
        );
    }
}
