package ru.cells.icc.tabbypdf.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.MultipartConfigElement;

/**
 * @author aaltaev
 * @since 0.1
 */
@SpringBootApplication(scanBasePackages = {"ru.cells.icc.tabbypdf.web"})
public class App extends WebMvcConfigurerAdapter {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("32MB");
        factory.setMaxRequestSize("32MB");
        return factory.createMultipartConfig();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins);
    }
}
