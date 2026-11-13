package br.com.igreja.voluntarios.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/volunteers", "/api/v1/volunteers/**").permitAll()
                        .requestMatchers("/api/v1/ministries", "/api/v1/ministries/**").permitAll()
                        .requestMatchers("/api/v1/events", "/api/v1/events/**").permitAll()
                        .requestMatchers("/api/v1/schedules", "/api/v1/schedules/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/v1/volunteers", "/api/v1/volunteers/**",
                        "/api/v1/ministries", "/api/v1/ministries/**",
                        "/api/v1/events", "/api/v1/events/**",
                        "/api/v1/schedules", "/api/v1/schedules/**"))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/volunteers", configuration);
        source.registerCorsConfiguration("/api/v1/volunteers/**", configuration);
        source.registerCorsConfiguration("/api/v1/ministries", configuration);
        source.registerCorsConfiguration("/api/v1/ministries/**", configuration);
        source.registerCorsConfiguration("/api/v1/events", configuration);
        source.registerCorsConfiguration("/api/v1/events/**", configuration);
        source.registerCorsConfiguration("/api/v1/schedules", configuration);
        source.registerCorsConfiguration("/api/v1/schedules/**", configuration);
        return source;
    }
}
