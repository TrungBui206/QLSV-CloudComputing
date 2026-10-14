
package hd.quanlysinhvien_hibernatedao.service;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;

public interface AcademicYearService {

    PageResponse<AcademicYearResponse> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction
    );

    AcademicYearResponse findById(Long id);

    AcademicYearResponse create(AcademicYearRequest request);

    AcademicYearResponse update(Long id, AcademicYearRequest request);

    void delete(Long id);
}
