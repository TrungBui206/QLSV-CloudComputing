
package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;

import static org.junit.jupiter.api.Assertions.*;

class ClassInfoMappingExpansionTest {

    private final ClassInfoMapper mapper =
            Mappers.getMapper(ClassInfoMapper.class);

    private ClassInfoRequest createRequestWithAllFields()
            throws ReflectiveOperationException {

        RecordComponent[] components =
                ClassInfoRequest.class.getRecordComponents();

        Class<?>[] parameterTypes =
                new Class<?>[components.length];

        Object[] values =
                new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];

            parameterTypes[i] = component.getType();

            assertEquals(
                    String.class,
                    component.getType(),
                    "Chưa có dữ liệu kiểm thử cho kiểu: "
                            + component.getType().getName()
            );

            values[i] = component.getName().equals("id")
                    ? "request-id"
                    : "value-" + component.getName();
        }

        Constructor<ClassInfoRequest> constructor =
                ClassInfoRequest.class.getDeclaredConstructor(
                        parameterTypes
                );

        return constructor.newInstance(values);
    }

    private Object getEntityValue(
            ClassInfo entity,
            String fieldName
    ) throws ReflectiveOperationException {

        String getterName =
                "get"
                        + Character.toUpperCase(fieldName.charAt(0))
                        + fieldName.substring(1);

        return ClassInfo.class
                .getMethod(getterName)
                .invoke(entity);
    }

    @Test
    void builder_canCreateRequest() {
        ClassInfoRequest request =
                ClassInfoRequest.builder()
                        .id("class-001")
                        .name("CSE2026")
                        .domain("Computer Science")
                        .description("Original")
                        .build();

        assertEquals("class-001", request.id());
        assertEquals("CSE2026", request.name());
        assertEquals("Computer Science", request.domain());
        assertEquals("Original", request.description());
    }

    @Test
    void toBuilder_canChangeFieldsWithoutChangingOriginal() {
        ClassInfoRequest original =
                ClassInfoRequest.builder()
                        .id("class-001")
                        .name("CSE2026")
                        .domain("Computer Science")
                        .description("Original")
                        .build();

        ClassInfoRequest modified =
                original.toBuilder()
                        .name("CSE2027")
                        .description("Updated")
                        .build();

        assertEquals("CSE2026", original.name());
        assertEquals("Original", original.description());

        assertEquals("class-001", modified.id());
        assertEquals("CSE2027", modified.name());
        assertEquals("Computer Science", modified.domain());
        assertEquals("Updated", modified.description());
    }

    @Test
    void mapper_automaticallyMapsEveryStringField()
            throws ReflectiveOperationException {

        ClassInfoRequest request =
                createRequestWithAllFields();

        ClassInfo entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertNull(entity.getId());

        for (RecordComponent component :
                ClassInfoRequest.class.getRecordComponents()) {

            if (component.getName().equals("id")) {
                continue;
            }

            Object expected =
                    component.getAccessor().invoke(request);

            Object actual =
                    getEntityValue(entity, component.getName());

            assertEquals(
                    expected,
                    actual,
                    "Ánh xạ sai thuộc tính: " + component.getName()
            );
        }
    }

    @Test
    void mapper_automaticallyUpdatesEveryStringField()
            throws ReflectiveOperationException {

        ClassInfoRequest request =
                createRequestWithAllFields();

        ClassInfo existing = new ClassInfo();
        existing.setId("database-id");
        existing.setName("Old Name");
        existing.setDomain("Old Domain");
        existing.setDescription("Old Description");

        mapper.updateEntity(request, existing);

        assertEquals("database-id", existing.getId());

        for (RecordComponent component :
                ClassInfoRequest.class.getRecordComponents()) {

            if (component.getName().equals("id")) {
                continue;
            }

            Object expected =
                    component.getAccessor().invoke(request);

            Object actual =
                    getEntityValue(existing, component.getName());

            assertEquals(
                    expected,
                    actual,
                    "Cập nhật sai thuộc tính: " + component.getName()
            );
        }
    }

    @Test
    void mapper_responseContainsAllCorrespondingValues()
            throws ReflectiveOperationException {

        ClassInfoRequest request =
                createRequestWithAllFields();

        ClassInfo entity = mapper.toEntity(request);
        entity.setId("database-id");

        ClassInfoResponse response =
                mapper.toResponse(entity);

        assertNotNull(response);

        for (RecordComponent component :
                ClassInfoResponse.class.getRecordComponents()) {

            Object expected =
                    getEntityValue(entity, component.getName());

            Object actual =
                    component.getAccessor().invoke(response);

            assertEquals(
                    expected,
                    actual,
                    "Response sai thuộc tính: " + component.getName()
            );
        }
    }

    @Test
    void contract_requestFieldsMustExistInEntityAndResponse()
            throws ReflectiveOperationException {

        for (RecordComponent requestField :
                ClassInfoRequest.class.getRecordComponents()) {

            String fieldName = requestField.getName();

            Field entityField =
                    ClassInfo.class.getDeclaredField(fieldName);

            RecordComponent responseField = null;

            for (RecordComponent component :
                    ClassInfoResponse.class.getRecordComponents()) {

                if (component.getName().equals(fieldName)) {
                    responseField = component;
                    break;
                }
            }

            assertNotNull(
                    responseField,
                    "Response thiếu thuộc tính: " + fieldName
            );

            assertEquals(
                    requestField.getType(),
                    entityField.getType(),
                    "Entity khác kiểu dữ liệu: " + fieldName
            );

            assertEquals(
                    requestField.getType(),
                    responseField.getType(),
                    "Response khác kiểu dữ liệu: " + fieldName
            );
        }
    }
}
