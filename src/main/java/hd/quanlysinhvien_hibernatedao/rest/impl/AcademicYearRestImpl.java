
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.rest.AcademicYearRest;
import hd.quanlysinhvien_hibernatedao.service.AcademicYearService;
import hd.quanlysinhvien_hibernatedao.security.authorization.AuthorizationPolicies;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
        name = "Quản lý niên khóa",
        description = "Các API quản lý thông tin niên khóa"
)

@RestController
@RequestMapping("/api/academic-years")
public class AcademicYearRestImpl implements AcademicYearRest {

    private final AcademicYearService academicYearService;

    public AcademicYearRestImpl(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<PageResponse<AcademicYearResponse>> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        PageResponse<AcademicYearResponse> result =
                academicYearService.search(
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction
                );

        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ALL_ROLES)
    public ResponseEntity<AcademicYearResponse> getById(Long id) {

        AcademicYearResponse academicYear =
                academicYearService.findById(id);

        return ResponseEntity.ok(academicYear);
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<AcademicYearResponse> create(
            AcademicYearRequest request
    ) {

        AcademicYearResponse academicYear =
                academicYearService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(academicYear);
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<AcademicYearResponse> update(
            Long id,
            AcademicYearRequest request
    ) {

        AcademicYearResponse academicYear =
                academicYearService.update(id, request);

        return ResponseEntity.ok(academicYear);
    }

    @Override
    @PreAuthorize(AuthorizationPolicies.ADMIN_ONLY)
    public ResponseEntity<Void> delete(Long id) {

        academicYearService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
