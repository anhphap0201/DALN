package com.example.ecommerceelectroniccomponentsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path to uploads directory
        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";
        
        // Serve uploaded images from /uploads/** path
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/uploads/**")
                .allowedOrigins("*")
                .allowedMethods("GET");
    }
}
