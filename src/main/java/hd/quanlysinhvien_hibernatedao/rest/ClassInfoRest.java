
package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ClassInfoRest {

    @PostMapping
    ResponseEntity<ClassInfoResponse> createClassInfo(
            @RequestBody ClassInfoRequest request
    );

    @PutMapping("/{id}")
    ResponseEntity<ClassInfoResponse> updateClassInfo(
            @PathVariable("id") String id,
            @RequestBody ClassInfoRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<String> deleteClassInfo(
            @PathVariable("id") String id
    );

    @GetMapping("/{id}")
    ResponseEntity<ClassInfoResponse> getClassInfoById(
            @PathVariable("id") String id
    );

    @GetMapping("/name/{name}")
    ResponseEntity<ClassInfoResponse> getClassInfoByName(
            @PathVariable("name") String name
    );

    @GetMapping
    ResponseEntity<List<ClassInfoResponse>> getAllClassInfo();

    @GetMapping("/domain/{domain}")
    ResponseEntity<List<ClassInfoResponse>> getClassInfoByDomain(
            @PathVariable("domain") String domain
    );

    @GetMapping("/search")
    ResponseEntity<List<ClassInfoResponse>> searchClassInfo(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "lowerLimit", required = false) Integer lowerLimit,
            @RequestParam(value = "upperLimit", required = false) Integer upperLimit,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "orderType", required = false) String orderType
    );

    @GetMapping("/count")
    ResponseEntity<Integer> getClassInfoCount(
            @RequestParam(value = "query", required = false) String query
    );
}
