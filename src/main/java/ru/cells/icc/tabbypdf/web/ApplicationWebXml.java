package ru.cells.icc.tabbypdf.web;

import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * @author aaltaev
 * @since 0.1
 */
public class ApplicationWebXml extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.bannerMode(Banner.Mode.OFF).sources(App.class);
    }
}
