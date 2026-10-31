package hd.quanlysinhvien_hibernatedao.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record AcademicYearRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @NotNull @Min(1900) @Max(2100) Integer startYear,
        @NotNull @Min(1900) @Max(2100) Integer endYear
) {
}
