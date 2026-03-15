package com.example.task_collaboration.infrastructure.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    
    @Value("${jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;
    
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;
    
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    
    public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        int accessTokenMaxAge = (int) (accessTokenExpiration / 1000);
        int refreshTokenMaxAge = (int) (refreshTokenExpiration / 1000);
        
        Cookie accessTokenCookie = createCookie(ACCESS_TOKEN_COOKIE, accessToken, accessTokenMaxAge);
        Cookie refreshTokenCookie = createCookie(REFRESH_TOKEN_COOKIE, refreshToken, refreshTokenMaxAge);
        
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
    
    public void clearTokenCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = createCookie(ACCESS_TOKEN_COOKIE, null, 0);
        Cookie refreshTokenCookie = createCookie(REFRESH_TOKEN_COOKIE, null, 0);
        
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
    
    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
    }
    
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getTokenFromCookie(request, REFRESH_TOKEN_COOKIE);
    }
    
    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
