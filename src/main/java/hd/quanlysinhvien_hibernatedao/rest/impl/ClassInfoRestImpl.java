
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.rest.ClassInfoRest;
import hd.quanlysinhvien_hibernatedao.service.ClassInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class ClassInfoRestImpl implements ClassInfoRest {

    private static final Logger logger =
            LoggerFactory.getLogger(ClassInfoRestImpl.class);

    private final ClassInfoService classInfoService;

    public ClassInfoRestImpl(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<ClassInfoResponse> createClassInfo(
            ClassInfoRequest request
    ) {
        logger.info("Creating ClassInfo");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classInfoService.create(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public ResponseEntity<ClassInfoResponse> updateClassInfo(
            String id,
            ClassInfoRequest request
    ) {
        logger.info("Updating ClassInfo: id={}", id);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu lớp học không hợp lệ"
            );
        }

        if (request.id() != null && !request.id().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID trong URL và Body không khớp"
            );
        }

        ClassInfoRequest updateRequest = new ClassInfoRequest(
                id,
                request.name(),
                request.domain(),
                request.description()
        );

        return ResponseEntity.ok(
                classInfoService.update(updateRequest)
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteClassInfo(String id) {
        logger.info("Deleting ClassInfo: id={}", id);

        classInfoService.delete(id);

        return ResponseEntity.ok(
                "Class Info deleted successfully"
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<ClassInfoResponse> getClassInfoById(
            String id
    ) {
        return ResponseEntity.ok(
                classInfoService.getById(id)
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<ClassInfoResponse> getClassInfoByName(
            String name
    ) {
        return ResponseEntity.ok(
                classInfoService.getByName(name)
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<List<ClassInfoResponse>> getAllClassInfo() {
        return ResponseEntity.ok(
                classInfoService.getAll()
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<List<ClassInfoResponse>> getClassInfoByDomain(
            String domain
    ) {
        return ResponseEntity.ok(
                classInfoService.getByDomain(domain)
        );
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    public ResponseEntity<List<ClassInfoResponse>> searchClassInfo(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType
    ) {
        return ResponseEntity.ok(
                classInfoService.search(
                        query,
                        lowerLimit,
                        upperLimit,
                        orderBy,
                        orderType
                )
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> getClassInfoCount(
            String query
    ) {
        return ResponseEntity.ok(
                classInfoService.getCount(query)
        );
    }
}
