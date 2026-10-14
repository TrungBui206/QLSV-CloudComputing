
package hd.quanlysinhvien_hibernatedao.dto.response;

import hd.quanlysinhvien_hibernatedao.entity.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String name,
        String email,
        LocalDate dob,
        Long departmentId,
        String departmentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getDob(),
                student.getDepartment().getId(),
                student.getDepartment().getName(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
