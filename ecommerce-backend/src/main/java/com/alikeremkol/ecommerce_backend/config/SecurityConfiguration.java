package com.alikeremkol.ecommerce_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final List<String> SECURED_ADMIN_URLS = List.of(
            "/api/v1/products/add",
            "/api/v1/products/product/*/update",
            "/api/v1/products/product/*/delete",
            "/api/v1/images/upload",
            "/api/v1/images/image/*/update",
            "/api/v1/images/image/*/delete",
            "/api/v1/categories/add",
            "/api/v1/categories/category/*/update",
            "/api/v1/categories/category/*/delete",
            "/api/v1/roles/**",
            "/api/v1/users/**",
            "/api/v1/orders/*/order-by-id",
            "/api/v1/orders/*/update-status",
            "/api/v1/orders/*/delete"); // SECURED_ADMIN_URLS.toArray(String[]::new)

    private static final List<String> SECURED_USER_URLS = List.of(
            "/api/v1/products/**",
            "/api/v1/images/**",
            "/api/v1/categories/**",
            "/api/v1/cart/**",
            "/api/v1/cartItems/**",
            "/api/v1/orders/**"); // SECURED_USER_URLS.toArray(String[]::new)


    private static final List<String> PUBLIC_URLS = List.of("/api/v1/auth/**"); // PUBLIC_URLS.toArray(String[]::new)


    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SECURED_ADMIN_URLS.toArray(String[]::new)).hasRole("ADMIN")
                        .requestMatchers(SECURED_USER_URLS.toArray(String[]::new)).hasAnyRole("USER", "ADMIN")
                        .requestMatchers(PUBLIC_URLS.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://app-backend.com", "http://localhost:8080")); //TODO: update backend url
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}