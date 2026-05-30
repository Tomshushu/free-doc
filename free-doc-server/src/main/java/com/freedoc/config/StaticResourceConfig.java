package com.freedoc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/upload/images/**")
                .addResourceLocations("file:" + uploadPath + "/images/")
                .resourceChain(true);

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .resourceChain(true);

        registry.addResourceHandler("/*.js", "/*.css", "/*.png", "/*.jpg", "/*.svg", "/*.ico", "/*.woff", "/*.woff2", "/*.ttf")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true);

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        if (!resourcePath.startsWith("api/")) {
                            return new ClassPathResource("/static/index.html");
                        }
                        return null;
                    }
                });
    }
}
