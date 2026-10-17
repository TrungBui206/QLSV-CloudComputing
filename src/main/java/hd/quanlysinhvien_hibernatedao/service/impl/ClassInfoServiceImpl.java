
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IClassInfoDao;
import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;
import hd.quanlysinhvien_hibernatedao.service.ClassInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClassInfoServiceImpl implements ClassInfoService {

    private static final Logger logger =
            LoggerFactory.getLogger(ClassInfoServiceImpl.class);

    private final IClassInfoDao classInfoDao;

    public ClassInfoServiceImpl(IClassInfoDao classInfoDao) {
        this.classInfoDao = classInfoDao;
    }

    private String requiredText(
            String value,
            String field,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " không được để trống"
            );
        }

        String result = value.trim();

        if (result.length() > maxLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " vượt quá " + maxLength + " ký tự"
            );
        }

        return result;
    }

    private String optionalDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String result = value.trim();

        if (result.length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mô tả không được vượt quá 1000 ký tự"
            );
        }

        return result;
    }

    private ClassInfo findExisting(String id) {
        String validId = requiredText(id, "ID", 36);

        return classInfoDao.findById(validId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy lớp học: " + validId
                        )
                );
    }

    private void validateRequest(ClassInfoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu lớp học không hợp lệ"
            );
        }

        requiredText(request.name(), "Tên lớp", 150);
        requiredText(request.domain(), "Domain", 120);
        optionalDescription(request.description());
    }

    private List<ClassInfoResponse> toResponses(
            List<ClassInfo> entities
    ) {
        return entities.stream()
                .map(ClassInfoResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ClassInfoResponse create(ClassInfoRequest request) {
        validateRequest(request);

        String name = request.name().trim();

        if (classInfoDao.findByNameIgnoreCase(name).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên lớp học đã tồn tại"
            );
        }

        ClassInfo entity = new ClassInfo();
        entity.setName(name);
        entity.setDomain(request.domain().trim());
        entity.setDescription(
                optionalDescription(request.description())
        );

        ClassInfo saved = classInfoDao.create(entity);
        classInfoDao.flush();

        logger.info(
                "Created ClassInfo: id={}, name={}",
                saved.getId(),
                saved.getName()
        );

        return ClassInfoResponse.from(saved);
    }

    @Override
    @Transactional
    public ClassInfoResponse update(ClassInfoRequest request) {
        validateRequest(request);

        String id = requiredText(request.id(), "ID", 36);
        ClassInfo existing = findExisting(id);

        String newName = request.name().trim();

        classInfoDao.findByNameIgnoreCase(newName)
                .ifPresent(duplicate -> {
                    if (!duplicate.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Tên lớp học đã tồn tại"
                        );
                    }
                });

        existing.setName(newName);
        existing.setDomain(request.domain().trim());
        existing.setDescription(
                optionalDescription(request.description())
        );

        ClassInfo updated = classInfoDao.update(existing);
        classInfoDao.flush();

        logger.info(
                "Updated ClassInfo: id={}, name={}",
                updated.getId(),
                updated.getName()
        );

        return ClassInfoResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(String id) {
        ClassInfo existing = findExisting(id);

        classInfoDao.delete(existing);
        classInfoDao.flush();

        logger.info(
                "Deleted ClassInfo: id={}, name={}",
                existing.getId(),
                existing.getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClassInfoResponse getById(String id) {
        return ClassInfoResponse.from(findExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassInfoResponse getByName(String name) {
        String validName = requiredText(name, "Tên lớp", 150);

        ClassInfo entity = classInfoDao
                .findByNameIgnoreCase(validName)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy lớp học: " + validName
                        )
                );

        return ClassInfoResponse.from(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassInfoResponse> getAll() {
        return toResponses(classInfoDao.getAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassInfoResponse> getByDomain(String domain) {
        String validDomain =
                requiredText(domain, "Domain", 120);

        return toResponses(
                classInfoDao.findByDomainIgnoreCase(validDomain)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassInfoResponse> search(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType
    ) {

        int from = lowerLimit == null ? 0 : lowerLimit;
        int to = upperLimit == null ? from + 10 : upperLimit;

        if (from < 0 || to <= from || to - from > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khoảng phân trang không hợp lệ"
            );
        }

        return toResponses(
                classInfoDao.search(
                        query,
                        from,
                        to,
                        orderBy,
                        orderType
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCount(String query) {
        return Math.toIntExact(classInfoDao.countSearch(query));
    }
}
