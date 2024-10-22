package com.ITSA.AdminProxy.config.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CorsResponseFilter implements Filter {
    private final static String allowedOrigins = "https://itsag1t1.com"; 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Access-Control-Allow-Origin", allowedOrigins);
        httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH, HEAD");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Cache-Control");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        // httpResponse.setHeader("Access-Control-Expose-Headers", "X-Auth-Token, Other-Custom-Headers");
        chain.doFilter(request, response);
    }

    // implement the rest of the required methods...
}
