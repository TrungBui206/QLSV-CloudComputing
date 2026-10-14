
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.rest.StudentRest;
import hd.quanlysinhvien_hibernatedao.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentRestImpl implements StudentRest {

    private final StudentService studentService;

    public StudentRestImpl(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public ResponseEntity<List<StudentResponse>> getAll() {

        List<StudentResponse> students = studentService.findAll();

        return ResponseEntity.ok(students);
    }

    @Override
    public ResponseEntity<PageResponse<StudentResponse>> search(
            String keyword,
            Long departmentId,
            int page,
            int size,
            String sortBy,
            String direction) {

        PageResponse<StudentResponse> result = studentService.search(
                keyword,
                departmentId,
                page,
                size,
                sortBy,
                direction
        );

        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<StudentResponse> getById(Long id) {

        StudentResponse student = studentService.findById(id);

        return ResponseEntity.ok(student);
    }

    @Override
    public ResponseEntity<StudentResponse> create(
            StudentRequest request) {

        StudentResponse student = studentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(student);
    }

    @Override
    public ResponseEntity<StudentResponse> update(
            Long id,
            StudentRequest request) {

        StudentResponse student = studentService.update(id, request);

        return ResponseEntity.ok(student);
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {

        studentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
