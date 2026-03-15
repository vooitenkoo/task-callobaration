package com.example.task_collaboration.infrastructure.config;

import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.infrastructure.service.CookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService, 
                                  JwtTokenProvider jwtTokenProvider,
                                  CookieService cookieService) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // Try to get token from cookie first, then from header
        String token = cookieService.getAccessTokenFromCookie(request);
        
        // Fallback to Authorization header
        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }

        if (token != null) {
            if (!jwtTokenProvider.validateToken(token)) {
                logger.warn("Invalid JWT token received");
                chain.doFilter(request, response);
                return;
            }

            String email = jwtTokenProvider.getEmailFromToken(token);
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (userDetails != null && userDetails instanceof CustomUserDetails) {
                    ((CustomUserDetails) userDetails).getId();
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                    if (authorities == null) {
                        logger.warn("Authorities is null for user: {}, using empty list", email);
                        authorities = Collections.emptyList();
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    logger.warn("UserDetails is null for email: {}", email);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
