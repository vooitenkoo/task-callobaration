package com.example.task_collaboration.infrastructure.oauth2;

import com.example.task_collaboration.domain.service.JwtService;
import com.example.task_collaboration.infrastructure.oauth2.dto.OAuth2LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        CustomOAuth2UserPrincipal oAuth2User = (CustomOAuth2UserPrincipal) authentication.getPrincipal();
        var user = oAuth2User.getUser();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        OAuth2LoginResponse loginResponse = new OAuth2LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getImageUrl(),
                user.getProvider().name(),
                user.getRole().name()
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Write JSON response
        objectMapper.writeValue(response.getWriter(), loginResponse);
        
        // Also set cookies for easier frontend handling
        response.addHeader("Set-Cookie", 
                "accessToken=" + accessToken + "; HttpOnly; Secure; SameSite=Strict; Max-Age=900");
        response.addHeader("Set-Cookie", 
                "refreshToken=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Max-Age=604800");
    }
}
