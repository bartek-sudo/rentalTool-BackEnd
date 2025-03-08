package com.example.rentalTool_BackEnd.user.security.jwt.filter;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.user.security.exception.InvalidCredentialsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                try {
                    final String token = authHeader.replace("Bearer ", "");
                    final Jwt jwt = jwtDecoder.decode(token);

                    // Check if token is expired
                    if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(now())) {
                        handleJwtException(response, new InvalidCredentialsException("Token expired"));
                        return;
                    }

                    final Collection<GrantedAuthority> authorities = parseAuthoritiesFromToken(jwt);

                    final Authentication authentication = new JwtAuthenticationToken(jwt, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }catch (JwtException | InvalidCredentialsException e) {
                    handleJwtException(response, e);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private Collection<GrantedAuthority> parseAuthoritiesFromToken(Jwt jwt) {
        return jwt.getClaimAsStringList("authorities").stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private void handleJwtException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), HttpResponse.builder()
                .httpStatus(UNAUTHORIZED)
                .statusCode(UNAUTHORIZED.value())
                .reason(UNAUTHORIZED.getReasonPhrase())
                .message(e.getMessage())
                .build());
    }
}
