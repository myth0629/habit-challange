package com.habitchallenge.backend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 인증 응답을 위한 DTO 클래스입니다.
 * 인증 성공 시 클라이언트에 반환되는 토큰과 사용자 정보를 포함합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    /** JWT 액세스 토큰 */
    private String token;
    
    /** 토큰 타입 (기본값: Bearer) */
    @Builder.Default
    private String type = "Bearer";
    
    /** 사용자 ID */
    private Long id;
    
    /** 사용자 이름 */
    private String nickname;
    
    /** 사용자 이메일 */
    private String email;
    
    /** 사용자 역할 */
    private String role;
    
    /** 오류 메시지 */
    private String errorMessage;
}
