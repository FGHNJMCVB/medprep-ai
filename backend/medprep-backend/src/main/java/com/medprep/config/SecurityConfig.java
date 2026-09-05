package com.medprep.config;

import com.medprep.security.CustomUserDetailsService;
import com.medprep.security.JwtRoleConverter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.env.Environment;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService) {

        this.userDetailsService = userDetailsService;
    }

    // ==========================================================
    // PASSWORD ENCODER
    // ==========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // ==========================================================
    // AUTHENTICATION PROVIDER
    // ==========================================================

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    // ==========================================================
    // AUTHENTICATION MANAGER
    // ==========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider) {

        return new ProviderManager(
                authenticationProvider
        );
    }

    // ==========================================================
    // JWT SECRET KEY
    // ==========================================================

    @Bean
    public SecretKey jwtSecretKey(
            Environment environment) {

        String secret =
                environment.getProperty(
                        "app.jwt.secret"
                );

        if (secret == null || secret.length() < 32) {

            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters"
            );
        }

        return new SecretKeySpec(
                secret.getBytes(
                        StandardCharsets.UTF_8
                ),
                "HmacSHA256"
        );
    }

    // ==========================================================
    // JWT ENCODER
    // ==========================================================

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey jwtSecretKey) {

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(
                        jwtSecretKey
                )
        );
    }

    // ==========================================================
    // JWT DECODER
    // ==========================================================

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSecretKey) {

        return NimbusJwtDecoder
                .withSecretKey(
                        jwtSecretKey
                )
                .macAlgorithm(
                        MacAlgorithm.HS256
                )
                .build();
    }

    // ==========================================================
    // JWT ROLE CONVERTER
    // ==========================================================

    @Bean
    public JwtRoleConverter jwtRoleConverter() {

        return new JwtRoleConverter();
    }

    // ==========================================================
    // CORS CONFIGURATION
    // ==========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5174"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // ==========================================================
    // SECURITY FILTER CHAIN
    // ==========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtRoleConverter jwtRoleConverter)
            throws Exception {

        http
                // ------------------------------------------------
                // CORS
                // ------------------------------------------------
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // ------------------------------------------------
                // CSRF
                // ------------------------------------------------
                .csrf(
                        AbstractHttpConfigurer::disable
                )

                // ------------------------------------------------
                // SESSION
                // ------------------------------------------------
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------
                .authorizeHttpRequests(auth -> auth

                        // ========================================
                        // CORS PREFLIGHT
                        // ========================================

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // ========================================
                        // PUBLIC
                        // ========================================

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/subjects/**",
                                "/api/topics/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/mock-tests/config"
                        ).permitAll()

                        .requestMatchers(
                                "/api/question-trends/**"
                        ).permitAll()

                        // ========================================
                        // ADMIN
                        // ========================================

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasAuthority(
                                "ROLE_ADMIN"
                        )

                        // ========================================
                        // AUTHENTICATED
                        // ========================================

                        .anyRequest().authenticated()
                )

                // ------------------------------------------------
                // JWT RESOURCE SERVER
                // ------------------------------------------------
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        jwt ->
                                                jwt.jwtAuthenticationConverter(
                                                        jwtRoleConverter
                                                )
                                )
                );

        return http.build();
    }
}