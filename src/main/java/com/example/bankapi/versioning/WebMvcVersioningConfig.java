package com.example.bankapi.versioning;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcVersioningConfig implements WebMvcConfigurer {

    private final DeprecationHeaderInterceptor deprecationHeaderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deprecationHeaderInterceptor)
                .addPathPatterns("/api/v1/**");
    }

}
