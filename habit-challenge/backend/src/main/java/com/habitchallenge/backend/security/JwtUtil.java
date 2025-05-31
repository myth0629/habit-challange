package com.habitchallenge.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    @Value("${jwt.token.prefix:Bearer }")
    private String tokenPrefix;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * UserDetails를 사용하여 토큰을 검증합니다.
     * 사용자 이름과 토큰 만료 여부를 확인합니다.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            return false;
        }
        
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("토큰 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
    
    public String resolveToken(String header) {
        if (header != null && header.startsWith(tokenPrefix)) {
            return header.substring(tokenPrefix.length());
        }
        return null;
    }
    
    /**
     * 토큰의 유효성만 검증합니다.
     * 사용자 정보 없이 토큰 자체의 유효성만 확인합니다.
     */
    public boolean validateToken(String token) {
        if (token == null) {
            logger.warn("토큰이 null입니다.");
            return false;
        }
        
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
                
            // 토큰이 만료되었는지 확인
            if (isTokenExpired(token)) {
                logger.warn("토큰이 만료되었습니다.");
                return false;
            }
            
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("유효하지 않은 JWT 토큰: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("JWT 토큰이 만료됨: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("지원되지 않는 JWT 토큰: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims 문자열이 비어있음: {}", ex.getMessage());
        } catch (SignatureException ex) {
            logger.error("유효하지 않은 JWT 서명: {}", ex.getMessage());
        } catch (Exception ex) {
            logger.error("JWT 토큰 검증 중 예상치 못한 오류: {}", ex.getMessage());
        }
        return false;
    }
}
