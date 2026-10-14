
package hd.quanlysinhvien_hibernatedao.dto.response;

import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;

import java.time.LocalDateTime;

public record AcademicYearResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer startYear,
        Integer endYear,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AcademicYearResponse from(AcademicYear academicYear) {
        return new AcademicYearResponse(
                academicYear.getId(),
                academicYear.getCode(),
                academicYear.getName(),
                academicYear.getDescription(),
                academicYear.getStartYear(),
                academicYear.getEndYear(),
                academicYear.getCreatedAt(),
                academicYear.getUpdatedAt()
        );
    }
}
