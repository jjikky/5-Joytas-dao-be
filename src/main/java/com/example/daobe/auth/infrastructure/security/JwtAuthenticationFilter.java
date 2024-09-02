package com.example.daobe.auth.infrastructure.security;

import com.example.daobe.auth.infrastructure.security.exception.SecurityException;
import com.example.daobe.auth.infrastructure.security.exception.SecurityExceptionType;
import com.example.daobe.auth.presentation.support.AuthHeaderExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public JwtAuthenticationFilter(RequestMatcher requestMatcher) {
        super(requestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException, IOException, ServletException {
        String accessToken = AuthHeaderExtractor.extract(request)
                .orElseThrow(() -> new SecurityException(SecurityExceptionType.UNAUTHORIZED));

        // 인증에 필요한 데이터 (인증 전의 토큰 객체)
        JwtAuthenticationToken beforeToken = JwtAuthenticationToken.beforeOf(accessToken);
        return super.getAuthenticationManager().authenticate(beforeToken);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            Authentication authResult
    ) throws IOException, ServletException {
        // 인증 성공시 -> Controller Handler
        // `SecurityContextHolder` 에 인증된 사용자 정보를 넣어야함
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        super.getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}
