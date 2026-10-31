package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.DocumentMetadataRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    Document toEntity(DocumentMetadataRequest request);

    DocumentResponse toResponse(Document document);

    List<DocumentResponse> toResponses(List<Document> documents);
}
