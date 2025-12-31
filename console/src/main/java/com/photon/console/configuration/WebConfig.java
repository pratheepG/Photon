package com.photon.console.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures resource handlers to serve static content from the React build.
     * *
     * The React build will be placed inside the Spring Boot project at
     * `src/main/resources/static/server-console/`.
     * *
     * This handler maps requests starting with `/server-console/` to the
     * `classpath:/static/server-console/` directory, allowing the browser
     * to load React's static assets (JS, CSS, images).
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/server-console/**")
                .addResourceLocations("classpath:/static/server-console/");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/server-console/assets/");
    }

    /**
     * Configures view controllers to handle routing for the React application.
     * *
     * This is crucial for single-page applications (SPAs) like React,
     * especially when using client-side routing (e.g., React Router).
     * *
     * IMPORTANT CHANGE: The `setViewName` now uses the *mapped* URL path
     * `/server-console/index.html` instead of the internal classpath path.
     * The `addResourceHandlers` will then correctly resolve this to
     * `classpath:/static/server-console/index.html`.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /* Map the base path /server-console to index.html */
        registry.addViewController("/server-console")
                .setViewName("forward:/server-console/index.html");

        /* Map any sub-path under /server-console (that doesn't have a file extension) */
        /* to index.html, allowing React Router to handle client-side routing. */
        registry.addViewController("/server-console/{path:[^\\.]*}")
                .setViewName("forward:/server-console/index.html");

    }
}