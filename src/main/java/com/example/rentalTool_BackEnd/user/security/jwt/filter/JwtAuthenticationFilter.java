package com.example.rentalTool_BackEnd.user.security.jwt.filter;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.security.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.time.Instant.now;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Pobierz JWT z cookie
        String token = getJwtFromCookies(request);
        if (token != null) {
            try {
                final Jwt jwt = jwtDecoder.decode(token);
                if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(now())) {
                    // Dla publicznych endpointów: ignoruj błąd, kontynuuj bez uwierzytelnienia
                    if (isPublicEndpoint(request)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    handleJwtException(response, new InvalidCredentialsException("Token expired"));
                    return;
                }
                final Collection<GrantedAuthority> authorities = parseAuthoritiesFromToken(jwt);
                final Authentication authentication = new JwtAuthenticationToken(jwt, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | InvalidCredentialsException e) {
                // Dla publicznych endpointów: ignoruj błąd, kontynuuj bez uwierzytelnienia
                if (isPublicEndpoint(request)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                handleJwtException(response, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Sprawdza czy endpoint jest publiczny (nie wymaga uwierzytelnienia)
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Publiczne endpointy
        return (path.startsWith("/api/v1/auth/register") && method.equals("POST")) ||
               (path.startsWith("/api/v1/auth/login") && method.equals("POST")) ||
               (path.startsWith("/api/v1/auth/verify-email") && method.equals("GET")) ||
               (path.startsWith("/api/v1/auth/resend-verification") && method.equals("POST")) ||
               (path.matches("/api/v1/user/\\d+") && method.equals("GET")) ||
               (path.matches("/api/v1/tools/\\d+") && method.equals("GET")) ||
               (path.startsWith("/api/v1/tools/search") && method.equals("GET")) ||
               (path.matches("/api/v1/tools/\\d+/availability") && method.equals("GET")) ||
               (path.matches("/api/v1/tools/\\d+/images") && method.equals("GET")) ||
               (path.startsWith("/api/v1/files/") && method.equals("GET")) ||
               (path.startsWith("/api/v1/terms/") && method.equals("GET")) ||
               (path.startsWith("/v3/api-docs/")) ||
               (path.startsWith("/swagger-ui/")) ||
               (path.equals("/swagger-ui.html"));
    }

    private Collection<GrantedAuthority> parseAuthoritiesFromToken(Jwt jwt) {
        return jwt.getClaimAsStringList("authorities").stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void handleJwtException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(APPLICATION_JSON_VALUE);
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), HttpResponse.builder()
                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                .httpStatus(UNAUTHORIZED)
                .statusCode(UNAUTHORIZED.value())
                .reason("Authorization failed")
                .message(e.getMessage() != null ? e.getMessage() : "Invalid or expired token")
                .build());
    }
}
