
package hd.quanlysinhvien_hibernatedao.service;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;

import java.util.List;

public interface StudentService {

    List<StudentResponse> findAll();

    PageResponse<StudentResponse> search(
            String keyword,
            Long departmentId,
            int page,
            int size,
            String sortBy,
            String direction
    );

    StudentResponse findById(Long id);

    StudentResponse create(StudentRequest request);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);
}
