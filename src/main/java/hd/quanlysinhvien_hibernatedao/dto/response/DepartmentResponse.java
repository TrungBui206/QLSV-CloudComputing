
package hd.quanlysinhvien_hibernatedao.dto.response;

import hd.quanlysinhvien_hibernatedao.entity.Department;

import java.time.LocalDateTime;

public record DepartmentResponse(
        Long id,
        String code,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
