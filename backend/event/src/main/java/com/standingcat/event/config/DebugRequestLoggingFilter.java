package com.standingcat.event.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class DebugRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("🔍 DebugFilter: Incoming request " + request.getMethod() + " " + request.getRequestURI());

        //check auth state before JwtAuthenticationFilter
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("🔍 DebugFilter: No authentication yet");
        } else {
            System.out.println("🔍 DebugFilter: Existing auth " +
                    SecurityContextHolder.getContext().getAuthentication().getName());
        }

        filterChain.doFilter(request, response);

        //Check auth state AFTER filters ran
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("🔍 DebugFilter: Still no authentication after chain");
        } else {
            System.out.println("🔍 DebugFilter: Authenticated user after chain = " +
                    SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }
}

