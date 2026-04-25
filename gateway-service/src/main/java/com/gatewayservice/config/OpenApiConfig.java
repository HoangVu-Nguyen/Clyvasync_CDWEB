package com.gatewayservice.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addServersItem(new Server().url("https://localhost:8443").description("Gateway Server"))
                .addServersItem(new Server().url("https://vcebook.io.vn").description("Production Server"))
                .info(new Info().title("Clyvasync API Documentation").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
    @Bean
    public OperationCustomizer addLocalizationHeader() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new HeaderParameter()
                    .name("Accept-Language")
                    .description("Ngôn ngữ phản hồi (vi, en, zh)")
                    .required(false)
                    .example("vi"));
            return operation;
        };
    }
}
