package org.example;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for web-related settings, including CORS and resource handling.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures the CORS settings for the application.
     *
     * @param registry The registry to which CORS mappings are added.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * Configures resource handlers for serving static files.
     *
     * @param registry The registry to which resource handlers are added.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/data/**")
                .addResourceLocations("file:./data/");
    }
}

