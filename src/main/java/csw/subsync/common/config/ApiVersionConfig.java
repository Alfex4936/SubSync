package csw.subsync.common.config;

import csw.subsync.common.annotation.ApiV1;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    public static final String API_V1_PREFIX = "/api/v1";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        PathPatternParser pathPatternParser = new PathPatternParser();
        pathPatternParser.setCaseSensitive(false);
        configurer.setPatternParser(pathPatternParser);

        // Apply the prefix only to controllers annotated with @ApiV1
        configurer.addPathPrefix(API_V1_PREFIX, c -> c.isAnnotationPresent(ApiV1.class));
    }
}