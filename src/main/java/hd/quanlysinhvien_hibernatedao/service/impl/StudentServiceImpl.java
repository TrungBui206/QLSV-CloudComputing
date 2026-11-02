package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.common.util.SearchUtils;
import hd.quanlysinhvien_hibernatedao.dao.IDepartmentDao;
import hd.quanlysinhvien_hibernatedao.dao.IStudentDao;
import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import hd.quanlysinhvien_hibernatedao.mapper.StudentMapper;
import hd.quanlysinhvien_hibernatedao.security.scope.StudentDataScope;
import hd.quanlysinhvien_hibernatedao.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "name", "email", "dob", "createdAt");

    private final IStudentDao studentDao;
    private final IDepartmentDao departmentDao;
    private final StudentMapper studentMapper;
    private final StudentDataScope studentDataScope;

    public StudentServiceImpl(
            IStudentDao studentDao,
            IDepartmentDao departmentDao,
            StudentMapper studentMapper,
            StudentDataScope studentDataScope
    ) {
        this.studentDao = studentDao;
        this.departmentDao = departmentDao;
        this.studentMapper = studentMapper;
        this.studentDataScope = studentDataScope;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        Optional<String> ownEmail = studentDataScope.restrictedEmail();

        // STUDENT: chi thay ho so cua chinh minh
        if (ownEmail.isPresent()) {
            return findOwn(ownEmail.get())
                    .map(student -> List.of(
                            studentMapper.toResponse(student)
                    ))
                    .orElse(List.of());
        }

        return studentMapper.toResponses(studentDao.getAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> search(
            String keyword,
            Long departmentId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Pageable pageable = SearchUtils.pageable(
                page, size, sortBy, direction, SORT_FIELDS
        );
        String kw = SearchUtils.keyword(keyword);

        Optional<String> ownEmail = studentDataScope.restrictedEmail();

        // STUDENT: chi tim trong ho so cua chinh minh
        if (ownEmail.isPresent()) {
            return PageResponse.from(
                    searchOwn(ownEmail.get(), kw, departmentId, pageable)
            );
        }
        Page<StudentResponse> result = studentDao
                .search(kw, departmentId, pageable)
                .map(studentMapper::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        Optional<String> ownEmail = studentDataScope.restrictedEmail();

        // STUDENT: chi duoc xem ho so cua chinh minh (khac => 403)
        if (ownEmail.isPresent()) {
            Student own = findOwn(ownEmail.get())
                    .filter(student -> student.getId().equals(id))
                    .orElseThrow(this::notOwnProfile);

            return studentMapper.toResponse(own);
        }

        return studentMapper.toResponse(getStudent(id));
    }

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentDao.existsByEmail(request.email())) {
            throw emailExists(request.email());
        }
        Department department = getDepartment(request.departmentId());
        Student student = studentMapper.toEntity(request);
        student.setDepartment(department);
        Student saved = studentDao.create(student);
        return studentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getStudent(id);
        if (studentDao.existsByEmailAndIdNot(request.email(), id)) {
            throw emailExists(request.email());
        }
        Department department = getDepartment(request.departmentId());
        studentMapper.updateEntity(request, student);
        student.setDepartment(department);
        Student updated = studentDao.update(student);
        return studentMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!studentDao.exists(id)) {
            throw studentNotFound(id);
        }
        studentDao.deleteByPk(id);
    }

    private Optional<Student> findOwn(String email) {
        return studentDao.findByEmailIgnoreCase(email);
    }

    private Page<StudentResponse> searchOwn(
            String email,
            String keyword,
            Long departmentId,
            Pageable pageable
    ) {
        List<StudentResponse> matched = findOwn(email)
                .filter(student ->
                        matchesFilter(student, keyword, departmentId)
                )
                .map(student -> List.of(
                        studentMapper.toResponse(student)
                ))
                .orElse(List.of());

        // Toi da 1 ban ghi nen chi nam o trang dau tien
        List<StudentResponse> content =
                pageable.getOffset() == 0 ? matched : List.of();

        return new PageImpl<>(content, pageable, matched.size());
    }

    private boolean matchesFilter(
            Student student,
            String keyword,
            Long departmentId
    ) {
        if (departmentId != null
                && (student.getDepartment() == null
                || !departmentId.equals(student.getDepartment().getId()))) {
            return false;
        }

        String normalized = SearchUtils.normalize(keyword);

        return normalized.isEmpty()
                || containsIgnoreCase(student.getName(), normalized)
                || containsIgnoreCase(student.getEmail(), normalized);
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private ResponseStatusException notOwnProfile() {
        return new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bạn chỉ được xem hồ sơ sinh viên của chính mình"
        );
    }

    private Student getStudent(Long id) {
        Student student = studentDao.get(id);
        if (student == null) {
            throw studentNotFound(id);
        }
        return student;
    }

    private Department getDepartment(Long departmentId) {
        Department department = departmentDao.get(departmentId);
        if (department == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Department không tồn tại: " + departmentId
            );
        }
        return department;
    }

    private ResponseStatusException studentNotFound(Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy student id = " + id
        );
    }

    private ResponseStatusException emailExists(String email) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email đã tồn tại: " + email
        );
    }
}
