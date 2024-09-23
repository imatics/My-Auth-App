package com.auth.app.configuration;

import com.nimbusds.jose.util.IOUtils;
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

@Order(value = Ordered.LOWEST_PRECEDENCE)
@Component
@WebFilter(filterName = "ResponseCachingFilter", urlPatterns = {"/api/*"})
class ResponseCachingFilter extends OncePerRequestFilter {

    private Logger LOGGER = Logger.getLogger(RequestCachingFilter.class.getName());

    private byte[]  cachedPayload = null;


    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException, IOException {
        if(request.getRequestURI().contains("api/")){
            ServletResponseWrapperCopier capturingResponseWrapper = new ServletResponseWrapperCopier((HttpServletResponse)response);
            filterChain.doFilter(request, capturingResponseWrapper);
            String str = capturingResponseWrapper.getCaptureAsString();
            LOGGER.info("==================RESPONSE==================");
            LOGGER.info("URL: "+request.getRequestURI());
            LOGGER.info("METHOD: "+request.getMethod());
            LOGGER.info("RESPONSE DATA: " + new String(capturingResponseWrapper.getCaptureAsBytes(), StandardCharsets.UTF_8));
            LOGGER.info("==================END RESPONSE==================\n\n");
            response.getOutputStream().write(str.getBytes());
        }else{
            filterChain.doFilter(request, response);

        }
    }
}