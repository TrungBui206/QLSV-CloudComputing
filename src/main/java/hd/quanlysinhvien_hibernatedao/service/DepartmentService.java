
package hd.quanlysinhvien_hibernatedao.service;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> findAll();

    PageResponse<DepartmentResponse> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction
    );

    DepartmentResponse findById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);
}
