package com.habitchallenge.backend.security;

import com.habitchallenge.backend.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Incoming request - Path: " + path + ", Method: " + method);
        
        // 인증이 필요없는 경로는 JWT 검증을 건너뜁니다.
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/auth/") ||
            path.startsWith("/api/users/register") ||
            path.startsWith("/users/register") ||
            path.startsWith("/api/challenges/all") ||
            path.startsWith("/challenges/all") ||
            path.equals("/api/ranking") || // 전체 랭킹 조회만 공개
            (path.startsWith("/api/ranking/") && !path.equals("/api/ranking/my-ranking")) || // 개인 랭킹 조회는 인증 필요
            (path.startsWith("/api/challenge-reviews") && !path.equals("/api/challenge-reviews/my")) ||
            path.equals("/error") ||
            method.equals("OPTIONS")) {
            logger.debug("Skipping JWT validation for public endpoint: " + path);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = parseJwt(request);
            logger.debug("JWT Token: " + (jwt != null ? "[HIDDEN]" : "null"));
            
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.extractUsername(jwt);
                logger.debug("Valid JWT token for user: " + username);
                
                UserDetails userDetails = userService.loadUserByUsername(username);
                
                // 사용자 세부 정보로 토큰 추가 검증
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Successfully set authentication for user: " + username);
                } else {
                    logger.warn("Token validation with user details failed for user: " + username);
                    // 토큰 검증 실패 시 401 응답
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"토큰이 유효하지 않습니다\"}");
                    return;
                }
            } else {
                logger.warn("No valid JWT token found");
                // 유효한 토큰이 없을 때 401 응답
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
                return;
            }
        } catch (Exception e) {
            String errorMsg = "사용자 인증 설정 중 오류가 발생했습니다: " + e.getMessage();
            logger.error(errorMsg, e);
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
}
