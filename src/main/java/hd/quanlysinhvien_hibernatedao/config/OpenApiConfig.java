package hd.quanlysinhvien_hibernatedao.config;

import hd.quanlysinhvien_hibernatedao.wrapper.ApiError;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String API_ERROR_SCHEMA =
            "#/components/schemas/ApiError";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Management System API")
                        .version("1.0.0")
                        .description(
                                "Tài liệu REST API hệ thống quản lý sinh viên"
                        ))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }

    @Bean
    public OpenApiCustomizer globalApiResponsesCustomizer() {
        return openApi -> {
            registerApiErrorSchema(openApi);

            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().forEach((path, pathItem) -> {
                if (!path.startsWith("/api/")) {
                    return;
                }

                pathItem.readOperationsMap().forEach((method, operation) -> {
                    boolean publicAuth =
                            isPublicAuthEndpoint(path, method);

                    if (!publicAuth) {
                        addBearerSecurity(operation);
                    }

                    addErrorResponses(
                            operation,
                            path,
                            method,
                            publicAuth
                    );
                });
            });
        };
    }

    private void registerApiErrorSchema(OpenAPI openApi) {
        Components components = openApi.getComponents();

        if (components == null) {
            components = new Components();
            openApi.setComponents(components);
        }

        Components targetComponents = components;

        ModelConverters.getInstance()
                .readAll(ApiError.class)
                .forEach(targetComponents::addSchemas);
    }

    private boolean isPublicAuthEndpoint(
            String path,
            PathItem.HttpMethod method
    ) {
        if (method != PathItem.HttpMethod.POST) {
            return false;
        }

        return "/api/auth/login".equals(path)
                || "/api/auth/register".equals(path);
    }

    private void addBearerSecurity(Operation operation) {
        boolean hasBearerSecurity =
                operation.getSecurity() != null
                        && operation.getSecurity()
                        .stream()
                        .anyMatch(requirement ->
                                requirement.containsKey(BEARER_AUTH));

        if (!hasBearerSecurity) {
            operation.addSecurityItem(
                    new SecurityRequirement().addList(BEARER_AUTH)
            );
        }
    }

    private void addErrorResponses(
            Operation operation,
            String path,
            PathItem.HttpMethod method,
            boolean publicAuth
    ) {
        ApiResponses responses = operation.getResponses();

        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        addResponseIfAbsent(
                responses,
                "400",
                "Yêu cầu hoặc dữ liệu đầu vào không hợp lệ"
        );

        if (!publicAuth) {
            responses.addApiResponse(
                    "401",
                    createErrorResponse(
                            "Chưa xác thực hoặc JWT không hợp lệ"
                    )
            );

            responses.addApiResponse(
                    "403",
                    createErrorResponse(
                            "Không có quyền thực hiện thao tác"
                    )
            );
        }

        if ("/api/auth/login".equals(path)
                && method == PathItem.HttpMethod.POST) {
            responses.addApiResponse(
                    "401",
                    createErrorResponse(
                            "Tên đăng nhập hoặc mật khẩu không hợp lệ"
                    )
            );
        }

        if (path.contains("{")) {
            addResponseIfAbsent(
                    responses,
                    "404",
                    "Không tìm thấy tài nguyên"
            );
        }

        if (method == PathItem.HttpMethod.POST
                || method == PathItem.HttpMethod.PUT
                || method == PathItem.HttpMethod.PATCH
                || method == PathItem.HttpMethod.DELETE) {
            addResponseIfAbsent(
                    responses,
                    "409",
                    "Dữ liệu trùng hoặc vi phạm ràng buộc"
            );
        }

        addResponseIfAbsent(
                responses,
                "500",
                "Lỗi xử lý phía máy chủ"
        );
    }

    private void addResponseIfAbsent(
            ApiResponses responses,
            String status,
            String description
    ) {
        if (!responses.containsKey(status)) {
            responses.addApiResponse(
                    status,
                    createErrorResponse(description)
            );
        }
    }

    private io.swagger.v3.oas.models.responses.ApiResponse createErrorResponse(
            String description
    ) {
        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(
                                "application/json",
                                new MediaType()
                                        .schema(new Schema<>()
                                                .$ref(API_ERROR_SCHEMA))
                        ));
    }
}