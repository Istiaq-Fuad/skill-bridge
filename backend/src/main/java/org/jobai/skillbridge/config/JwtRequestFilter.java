package org.jobai.skillbridge.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jobai.skillbridge.service.UserService;
import org.jobai.skillbridge.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final String authorizationHeader = request.getHeader("Authorization");

        // Skip JWT filter for login and register endpoints
        if (requestURI.contains("/api/users/login") || requestURI.contains("/api/users/register")) {
            chain.doFilter(request, response);
            return;
        }

        String username = null;
        String jwt = null;

        // First try Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        } else {
            // Fallback to cookie
            if (request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        System.out.println(
                                "Found JWT in cookie: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
                        break;
                    }
                }
            }
        }

        // Check if JWT token is not empty or just whitespace
        if (jwt != null && jwt.trim().length() > 0) {
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                System.out.println("Extracted username from JWT: " + username);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (MalformedJwtException e) {
                System.out.println("Malformed JWT Token: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (io.jsonwebtoken.security.SignatureException e) {
                System.out.println(
                        "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted: "
                                + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (Exception e) {
                System.out.println("JWT Token parsing error: " + e.getMessage());
                if (e.getMessage().contains("JWT signature does not match")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, (org.jobai.skillbridge.model.User) userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
