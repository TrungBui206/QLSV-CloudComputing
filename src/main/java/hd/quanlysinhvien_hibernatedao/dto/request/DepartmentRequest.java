
package hd.quanlysinhvien_hibernatedao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description
) {
}
