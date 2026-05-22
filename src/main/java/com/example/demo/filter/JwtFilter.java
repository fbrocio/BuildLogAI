package com.example.demo.filter;

import com.example.demo.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService){
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String header = req.getHeader("Authorization");

        if(header != null && header.startsWith("Bearer ")){
            String token = header.substring(7);

            if(jwtService.isValid(token)){
                Long userId = jwtService.extractUserId(token);

                // Aquí podrías guardar el userId en contexto (opcional por ahora)
                req.setAttribute("userId", userId);
            }
        }

        chain.doFilter(request, response);
    }
}