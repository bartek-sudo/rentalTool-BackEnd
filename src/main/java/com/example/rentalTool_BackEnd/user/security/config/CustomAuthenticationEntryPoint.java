package com.example.rentalTool_BackEnd.user.security.config;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(UNAUTHORIZED.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        
        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                .httpStatus(UNAUTHORIZED)
                .statusCode(UNAUTHORIZED.value())
                .reason("Authorization failed")
                .message("Unauthorized access - authentication required")
                .build();
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), httpResponse);
    }
}


