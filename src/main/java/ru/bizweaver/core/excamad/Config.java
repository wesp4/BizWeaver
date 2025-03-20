package ru.bizweaver.core.excamad;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Configuration
public class Config {
    @Bean
    public FilterRegistrationBean processCorsFilter(CorsConfigurationSource corsConfigurationSource) {
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(corsConfigurationSource));
        bean.setOrder(0);
        return bean;
    }

    @Bean
    @Primary
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8080"));
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
