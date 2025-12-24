package ru.Edje_7.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestPath = request.getRequestURI();
        log.debug("JWT Filter processing request: {} {}", request.getMethod(), requestPath);

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || authHeader.isBlank()) {
            log.debug("No Authorization header found for request: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Authorization header does not start with 'Bearer ' for request: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX_LENGTH).trim();
        
        if (jwt.isEmpty()) {
            log.warn("JWT token is empty in Authorization header for request: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String username;

        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {}", username);
                } else {
                    log.warn("Invalid or expired JWT token for user: {}", username);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            }

        filterChain.doFilter(request, response);
    }
}
