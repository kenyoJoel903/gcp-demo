package pe.financieraoh.gcp.demo.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;


import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public GroupedOpenApi publicApiGroup() {
        return GroupedOpenApi.builder()
                .group("public-api-group")
                .pathsToMatch("/api/**")
                .build();
    }

    private Info apiInfo() {
        return new Info()
                .title("API REST services")
                .description("Especificacion de REST API services")
                .license(new License().name("Financiera Oh").url("https://tarjetaoh.pe"))
                .version("1.0");
    }

  

}
