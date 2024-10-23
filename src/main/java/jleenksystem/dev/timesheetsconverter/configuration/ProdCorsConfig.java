package jleenksystem.dev.timesheetsconverter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("prod")
public class ProdCorsConfig {

	@Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://time-sheets-converter.jleenksystem.dev")  // Production origin
                        .allowedMethods("GET", "POST", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
	
}
