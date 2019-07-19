package org.cyanspring.tools.third.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@Configuration  // 配置注解，自动在本类上下文加载一些环境变量信息
@EnableSwagger2 // 使swagger2生效
@EnableWebMvc
@ComponentScan(basePackages = {"org.cyanspring.tools.controller"})  //需要扫描的包路径
public class SwaggerConfig {
    @Bean
    public Docket learnyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()  // 选择哪些些路径和api会生成document
                .apis(RequestHandlerSelectors.any())  // 对所有api进行监控
                .paths(PathSelectors.any())  // 对所有路径进行监控
                .build();
    }

    private List<SecurityContext> securityContext() {
        AuthorizationScope[] authScopes = new AuthorizationScope[1];
        authScopes[0] = new AuthorizationScopeBuilder()
                .scope("read")
                .description("read access")
                .build();
        SecurityReference securityReference = SecurityReference.builder()
                .reference("Authorization")
                .scopes(authScopes)
                .build();

        List<SecurityContext> securityContexts = Arrays.asList((SecurityContext.builder().securityReferences
                (Arrays.asList((securityReference))).build()));
        return securityContexts;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("BitCoke Api v1.0")
                .description(" 2018-11-15 : 1.0版本  \n <br> ")
                .termsOfServiceUrl("http://springfox.io")
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
                .version("2.0")
                .build();
    }
}
