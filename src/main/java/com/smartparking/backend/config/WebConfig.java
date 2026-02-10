package com.smartparking.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull; // Import this
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) { // Add @NonNull here
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}