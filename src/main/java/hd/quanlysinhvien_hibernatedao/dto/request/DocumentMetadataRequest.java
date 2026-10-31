package hd.quanlysinhvien_hibernatedao.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentMetadataRequest(
        String appId,
        String name,
        String path,
        String owner,
        Double version,
        Boolean deleted,
        Boolean active
) {
}
