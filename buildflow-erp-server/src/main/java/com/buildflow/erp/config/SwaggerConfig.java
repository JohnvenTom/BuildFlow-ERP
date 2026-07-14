package com.buildflow.erp.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Knife4j API文档配置类
 * 配置Swagger2文档，提供接口文档和在线调试功能
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfig {

    /**
     * 配置API文档Docket
     * @return Docket Swagger配置对象
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.buildflow.erp.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 构建API文档基本信息
     * @return ApiInfo 文档信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("BuildFlow ERP 建材进销存CRM系统 API文档")
                .description("建材行业一体化进销存CRM系统后端接口文档")
                .version("1.0.0")
                .build();
    }

    /**
     * 配置安全方案（JWT Bearer Token）
     * @return 安全方案列表
     */
    private List<springfox.documentation.service.SecurityScheme> securitySchemes() {
        List<springfox.documentation.service.SecurityScheme> schemes = new ArrayList<>();
        schemes.add(new ApiKey("Authorization", "Authorization", "header"));
        return schemes;
    }

    /**
     * 配置安全上下文
     * @return 安全上下文列表
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> contexts = new ArrayList<>();
        contexts.add(SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("^(?!auth).*$"))
                .build());
        return contexts;
    }

    /**
     * 默认安全引用
     * @return 安全引用列表
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> references = new ArrayList<>();
        references.add(new SecurityReference("Authorization", authorizationScopes));
        return references;
    }
}
