
package hd.quanlysinhvien_hibernatedao.service;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;

import java.util.List;

public interface ClassInfoService {

    ClassInfoResponse create(ClassInfoRequest request);

    ClassInfoResponse update(ClassInfoRequest request);

    void delete(String id);

    ClassInfoResponse getById(String id);

    ClassInfoResponse getByName(String name);

    List<ClassInfoResponse> getAll();

    List<ClassInfoResponse> getByDomain(String domain);

    List<ClassInfoResponse> search(
            String query,
            Integer lowerLimit,
            Integer upperLimit,
            String orderBy,
            String orderType
    );

    Integer getCount(String query);
}
