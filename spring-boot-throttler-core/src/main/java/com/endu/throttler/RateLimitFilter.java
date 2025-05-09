package com.endu.throttler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = extractClientId(request);

        if (!isAllowed(clientId)) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            log.debug("Rate limit exceeded for client: {}", clientId);
            return;
        }

        log.debug("Rate limit allowed for client: {}", clientId);
        filterChain.doFilter(request, response);
    }

    private String extractClientId(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private boolean isAllowed(String clientId) {
        return rateLimiter.isAllowed(clientId);
    }
}
