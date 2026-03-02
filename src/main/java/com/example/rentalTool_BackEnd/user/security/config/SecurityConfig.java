package com.example.rentalTool_BackEnd.user.security.config;

import com.example.rentalTool_BackEnd.user.security.jwt.filter.JwtAuthenticationFilter;
import com.example.rentalTool_BackEnd.user.security.provider.AccountAuthenticationProvider;
import com.example.rentalTool_BackEnd.user.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
class SecurityConfig {

    private final AccountAuthenticationProvider authenticationProvider;
    private final CustomUserDetailsService detailsService;
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = security.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        authenticationManagerBuilder.userDetailsService(detailsService);

        security.csrf(AbstractHttpConfigurer::disable);

        security.oauth2ResourceServer(o2auth -> o2auth
                .jwt(jwtConfigurer -> {
                    jwtConfigurer.decoder(jwtDecoder);
                    jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter());
                })
                .authenticationEntryPoint(authenticationEntryPoint));

        security.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));

        security.authorizeHttpRequests(
                        auth ->
                                auth
                                        .requestMatchers(POST, "/api/v1/auth/register").permitAll()
                                        .requestMatchers(POST, "/api/v1/auth/login").permitAll()
                                        .requestMatchers(GET, "/api/v1/auth/verify-email").permitAll()
                                        .requestMatchers(POST, "/api/v1/auth/resend-verification").permitAll()
                                        .requestMatchers(GET, "/api/v1/auth/me").authenticated()
                                        .requestMatchers(POST, "/api/v1/auth/change-password").authenticated()
                                        .requestMatchers(POST, "/api/v1/auth/logout").authenticated()

                                        .requestMatchers(GET, "/api/v1/user/{id}").permitAll()
                                        .requestMatchers(GET, "/api/v1/user/admin").hasAuthority("ADMIN")
                                        .requestMatchers(PUT, "/api/v1/user/me").authenticated()
                                        .requestMatchers(PATCH, "/api/v1/user/admin/{id}/block").hasAuthority("ADMIN")
                                        .requestMatchers(PATCH, "/api/v1/user/admin/{id}/unblock").hasAuthority("ADMIN")
                                        .requestMatchers(PATCH, "/api/v1/user/admin/{id}/role").hasAuthority("ADMIN")

                                        // Tools endpoints
                                        .requestMatchers(POST, "/api/v1/tools").authenticated()
                                        .requestMatchers(PUT, "/api/v1/tools/{id}").authenticated()
                                        .requestMatchers(PATCH, "/api/v1/tools/{id}/status").authenticated()
                                        .requestMatchers(GET, "/api/v1/tools/{id}").permitAll()
                                        .requestMatchers(GET, "/api/v1/tools/search").permitAll()
                                        .requestMatchers(GET, "/api/v1/tools/{toolId}/availability").permitAll()

                                        // Tool images endpoints
                                        .requestMatchers(POST, "/api/v1/tools/{toolId}/images").authenticated()
                                        .requestMatchers(GET, "/api/v1/tools/{toolId}/images").permitAll()
                                        .requestMatchers(PUT, "/api/v1/tools/{toolId}/images/{imageId}/main").authenticated()
                                        .requestMatchers(DELETE, "/api/v1/tools/{toolId}/images/{imageId}").authenticated()
                                        .requestMatchers(GET, "/api/v1/files/{fileName:.+}").permitAll()

                                        // Reservation endpoints
                                        .requestMatchers(POST, "/api/v1/reservations").authenticated()
                                        .requestMatchers(GET, "/api/v1/reservations/my-rentals").authenticated()
                                        .requestMatchers(GET, "/api/v1/reservations/my-tools-reservations").authenticated()
                                        .requestMatchers(PUT, "/api/v1/reservations/{reservationId}/confirm").authenticated()
                                        .requestMatchers(PUT, "/api/v1/reservations/{reservationId}/accept-regulations").authenticated()
                                        .requestMatchers(PUT, "/api/v1/reservations/{reservationId}/cancel").authenticated()
                                        .requestMatchers(GET, "/api/v1/reservations/{reservationId}").authenticated()
                                        .requestMatchers(GET, "/api/v1/reservations/all").hasAuthority("MODERATOR")
                                        
                                        // Terms endpoints
                                        .requestMatchers(GET, "/api/v1/terms/**").permitAll()
                                        .requestMatchers(POST, "/api/v1/terms").hasAuthority("ADMIN")
                                        .requestMatchers(PUT, "/api/v1/terms/{id}").hasAuthority("ADMIN")
                                        .requestMatchers(DELETE, "/api/v1/terms/{id}").hasAuthority("ADMIN")

                                        // Category endpoints
                                        .requestMatchers(GET, "/api/v1/categories/**").permitAll()
                                        .requestMatchers(POST, "/api/v1/categories").hasAuthority("ADMIN")
                                        .requestMatchers(PUT, "/api/v1/categories/{id}").hasAuthority("ADMIN")
                                        .requestMatchers(DELETE, "/api/v1/categories/{id}").hasAuthority("ADMIN")

                                        // Endpoints dla moderatora
                                        .requestMatchers(GET, "/api/v1/moderation/status/{status}").hasAnyAuthority("MODERATOR")
                                        .requestMatchers(POST, "/api/v1/moderation/{toolId}/approve").hasAnyAuthority("MODERATOR")
                                        .requestMatchers(POST, "/api/v1/moderation/{toolId}/reject").hasAnyAuthority("MODERATOR")

                                        .requestMatchers("/v3/api-docs/**").permitAll()
                                        .requestMatchers("/swagger-ui/**").permitAll()
                                        .requestMatchers("/swagger-ui.html").permitAll()


                )
                .authenticationManager(authenticationManagerBuilder.build())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return security.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities"); // default is "roles"
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // default is "ROLE_"

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
