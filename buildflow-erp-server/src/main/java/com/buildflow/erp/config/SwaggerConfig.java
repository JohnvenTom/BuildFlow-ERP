package com.buildflow.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 4.x / OpenAPI 3 文档配置类
 * 替代原 Swagger 2 配置，基于 springdoc-openapi 提供 API 文档与在线调试
 *
 * <p>访问地址：http://localhost:8080/api/doc.html</p>
 */
@Configuration
public class SwaggerConfig {

    /** JWT 认证方案名称 */
    private static final String SECURITY_SCHEME_NAME = "Authorization";

    /**
     * 配置 OpenAPI 文档
     * @description 构建 OpenAPI 3 文档，包含 API 基本信息、JWT Bearer 认证方案
     * @return OpenAPI 文档配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()));
    }

    /**
     * 构建API文档基本信息
     * @return Info 文档信息
     */
    private Info apiInfo() {
        return new Info()
                .title("BuildFlow ERP 建材进销存CRM系统 API文档")
                .description("建材行业一体化进销存CRM系统后端接口文档")
                .version("1.0.0");
    }

    /**
     * 配置JWT Bearer Token认证方案
     * @description 通过请求头 Authorization 传递 Bearer Token 进行认证
     * @return SecurityScheme 安全方案
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(SECURITY_SCHEME_NAME);
    }
}
