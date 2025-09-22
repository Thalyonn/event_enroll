package com.standingcat.event.security.jwt;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = null;
        String authHeader = request.getHeader("Authorization");
        System.out.println("checking auth header");

        //try Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            System.out.println("setting jwt");
            jwt = authHeader.substring(7);
        }
        System.out.println("not found trying cookies");
        //if not found, try cookies
        if (jwt == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Checking cookie " + cookie);
                if ("jwt".equals(cookie.getName())) {
                    System.out.println("Setting cookie " + cookie.getValue());
                    jwt = cookie.getValue();
                }
            }
        }

        System.out.println("no token, continuing");

        //If no token, just continue
        if (jwt == null) {
            System.out.println("jwt is null");
            filterChain.doFilter(request, response);
            return;
        }

        final String username = jwtUtil.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("checking token validation");
            if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("jwtutil validate token is true");
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        System.out.println("doing filter");
        filterChain.doFilter(request, response);
    }

}
