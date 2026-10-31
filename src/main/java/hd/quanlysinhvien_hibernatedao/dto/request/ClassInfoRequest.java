
package hd.quanlysinhvien_hibernatedao.dto.request;

import lombok.Builder;

@Builder(toBuilder = true)
public record ClassInfoRequest(
        String id,
        String name,
        String domain,
        String description
) {
}
