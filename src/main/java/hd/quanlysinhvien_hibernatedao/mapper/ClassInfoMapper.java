
package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ClassInfoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "name",
            source = "name",
            qualifiedByName = "trimText"
    )
    @Mapping(
            target = "domain",
            source = "domain",
            qualifiedByName = "trimText"
    )
    @Mapping(
            target = "description",
            source = "description",
            qualifiedByName = "normalizeDescription"
    )
    ClassInfo toEntity(ClassInfoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "name",
            source = "name",
            qualifiedByName = "trimText"
    )
    @Mapping(
            target = "domain",
            source = "domain",
            qualifiedByName = "trimText"
    )
    @Mapping(
            target = "description",
            source = "description",
            qualifiedByName = "normalizeDescription"
    )
    void updateEntity(
            ClassInfoRequest request,
            @MappingTarget ClassInfo entity
    );

    ClassInfoResponse toResponse(ClassInfo entity);

    List<ClassInfoResponse> toResponses(List<ClassInfo> entities);

    @Named("trimText")
    default String trimText(String value) {
        return value == null ? null : value.trim();
    }

    @Named("normalizeDescription")
    default String normalizeDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
