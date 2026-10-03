package com.slotbook.web;

import com.slotbook.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor auth;
    @Value("${app.cors.origin}") private String origins;
    public WebConfig(AuthInterceptor auth) { this.auth = auth; }

    @Override public void addInterceptors(InterceptorRegistry r) { r.addInterceptor(auth).addPathPatterns("/api/bookings/**"); }
    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**").allowedOrigins(origins.split(",")).allowedMethods("*").allowedHeaders("*");
    }
}