package com.habitchallenge.backend.user.repository;

import com.habitchallenge.backend.user.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 비밀번호 재설정 토큰 레포지토리
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * 토큰으로 비밀번호 재설정 토큰 조회
     * @param token 토큰
     * @return 비밀번호 재설정 토큰
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * 사용자 ID로 유효한 토큰 조회
     * @param userId 사용자 ID
     * @return 비밀번호 재설정 토큰
     */
    Optional<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
} 