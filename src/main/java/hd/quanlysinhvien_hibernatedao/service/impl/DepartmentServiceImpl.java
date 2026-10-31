package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.common.util.SearchUtils;
import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.mapper.DepartmentMapper;
import hd.quanlysinhvien_hibernatedao.service.DepartmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "code", "name", "createdAt");

    private final IDepartmentDao departmentDao;
    private final IStudentDao studentDao;
    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(
            IDepartmentDao departmentDao,
            IStudentDao studentDao,
            DepartmentMapper departmentMapper
    ) {
        this.departmentDao = departmentDao;
        this.studentDao = studentDao;
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentMapper.toResponses(
                departmentDao.findAllSorted("name", "asc")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> search(
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Pageable pageable = SearchUtils.pageable(
                page, size, sortBy, direction, SORT_FIELDS
        );
        String kw = SearchUtils.keyword(keyword);
        Page<DepartmentResponse> result = departmentDao
                .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                        kw, kw, pageable
                )
                .map(departmentMapper::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return departmentMapper.toResponse(getDepartment(id));
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentDao.existsByCode(request.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã khoa đã tồn tại: " + request.code()
            );
        }
        Department department = departmentMapper.toEntity(request);
        Department saved = departmentDao.create(department);
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        if (departmentDao.existsByCodeAndIdNot(request.code(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã khoa đã tồn tại: " + request.code()
            );
        }
        departmentMapper.updateEntity(request, department);
        Department updated = departmentDao.update(department);
        return departmentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getDepartment(id);
        if (studentDao.existsByDepartmentId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Khoa đang có sinh viên, không thể xóa"
            );
        }
        departmentDao.deleteByPk(id);
    }

    private Department getDepartment(Long id) {
        Department department = departmentDao.get(id);
        if (department == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khoa id = " + id
            );
        }
        return department;
    }
}
