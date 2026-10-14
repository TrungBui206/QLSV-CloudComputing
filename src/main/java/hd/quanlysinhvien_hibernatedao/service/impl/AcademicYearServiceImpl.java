
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.common.util.SearchUtils;
import hd.quanlysinhvien_hibernatedao.dao.IAcademicYearDao;
import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;
import hd.quanlysinhvien_hibernatedao.service.AcademicYearService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AcademicYearServiceImpl implements AcademicYearService {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "code", "name", "startYear", "endYear", "createdAt");

    private final IAcademicYearDao academicYearDao;

    public AcademicYearServiceImpl(IAcademicYearDao academicYearDao) {
        this.academicYearDao = academicYearDao;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AcademicYearResponse> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction) {

        Pageable pageable = SearchUtils.pageable(
                page, size, sortBy, direction, SORT_FIELDS
        );

        String kw = SearchUtils.keyword(keyword);

        Page<AcademicYearResponse> result = academicYearDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        kw, kw, pageable
                )
                .map(AcademicYearResponse::from);

        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse findById(Long id) {

        AcademicYear academicYear = getAcademicYear(id);

        return AcademicYearResponse.from(academicYear);
    }

    @Override
    @Transactional
    public AcademicYearResponse create(AcademicYearRequest request) {

        checkYears(request);

        if (academicYearDao.existsByCode(request.code())) {
            throw codeExists(request.code());
        }

        AcademicYear academicYear = new AcademicYear();

        apply(academicYear, request);

        AcademicYear saved = academicYearDao.create(academicYear);

        return AcademicYearResponse.from(saved);
    }

    @Override
    @Transactional
    public AcademicYearResponse update(
            Long id,
            AcademicYearRequest request) {

        AcademicYear academicYear = getAcademicYear(id);

        checkYears(request);

        if (academicYearDao.existsByCodeAndIdNot(request.code(), id)) {
            throw codeExists(request.code());
        }

        apply(academicYear, request);

        AcademicYear updated = academicYearDao.update(academicYear);

        return AcademicYearResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        if (!academicYearDao.exists(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy niên khóa id = " + id
            );
        }

        academicYearDao.deleteByPk(id);
    }

    private AcademicYear getAcademicYear(Long id) {

        AcademicYear academicYear = academicYearDao.get(id);

        if (academicYear == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy niên khóa id = " + id
            );
        }

        return academicYear;
    }

    private void checkYears(AcademicYearRequest request) {

        if (request.endYear() < request.startYear()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Năm kết thúc không được nhỏ hơn năm bắt đầu"
            );
        }
    }

    private void apply(
            AcademicYear academicYear,
            AcademicYearRequest request) {

        academicYear.setCode(request.code());
        academicYear.setName(request.name());
        academicYear.setDescription(request.description());
        academicYear.setStartYear(request.startYear());
        academicYear.setEndYear(request.endYear());
    }

    private ResponseStatusException codeExists(String code) {

        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Mã niên khóa đã tồn tại: " + code
        );
    }
}
