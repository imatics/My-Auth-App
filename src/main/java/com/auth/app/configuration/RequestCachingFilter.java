package com.auth.app.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
@WebFilter(filterName = "RequestCachingFilter", urlPatterns = {"/api/*"})
class RequestCachingFilter extends OncePerRequestFilter {
    private Logger LOGGER = Logger.getLogger(RequestCachingFilter.class.getName());


    @Override
    public void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws IOException, ServletException {
        if(request.getRequestURI().contains("api/")){
        CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest(request);
        LOGGER.info("==================REQUEST==================");
        LOGGER.info("URL: "+request.getRequestURI());
        LOGGER.info("METHOD: "+request.getMethod());
        LOGGER.info("REQUEST DATA: \n "+ new String(cachedHttpServletRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        LOGGER.info("==================END REQUEST==================\n\n");
        filterChain.doFilter(cachedHttpServletRequest, response);
        }else{
            filterChain.doFilter(request, response);

        }
    }


}


