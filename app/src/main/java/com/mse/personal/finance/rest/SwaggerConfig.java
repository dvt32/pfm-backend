package com.mse.personal.finance.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * This class configures and enables Swagger 2 
 * (a generated REST API documentation with a UI).
 * 
 * Swagger UI can be accessed at <APP-URL>/swagger-ui.html
 * 
 * @author dvt32
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.any())              
          .paths(PathSelectors.any())                          
          .build();                                           
    }
    
}