package com.habitchallenge.backend.security.service;

import com.habitchallenge.backend.auth.dto.FindIdRequestDto;
import com.habitchallenge.backend.auth.dto.PasswordResetConfirmDto;
import com.habitchallenge.backend.auth.dto.PasswordResetRequestDto;
import com.habitchallenge.backend.email.EmailService;
import com.habitchallenge.backend.security.JwtUtil;
import com.habitchallenge.backend.security.dto.JwtResponse;
import com.habitchallenge.backend.security.dto.LoginRequest;
import com.habitchallenge.backend.user.domain.PasswordResetToken;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.repository.PasswordResetTokenRepository;
import com.habitchallenge.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        log.debug("로그인 시도: 이메일={}", loginRequest.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            log.debug("인증 성공: {}", authentication.getName());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = userService.getUserByEmail(loginRequest.getEmail());
            log.debug("사용자 정보 조회 성공: id={}, nickname={}", user.getId(), user.getNickname());
            
            String jwt = jwtUtil.generateToken(user);
            log.debug("JWT 토큰 생성 완료");
            
            return JwtResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
        } catch (BadCredentialsException e) {
            log.error("인증 실패: 잘못된 자격 증명", e);
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 아이디 찾기
     * @param request 아이디 찾기 요청
     */
    @Transactional
    public void findId(FindIdRequestDto request) {
        log.debug("아이디 찾기 요청: 이메일={}", request.getEmail());
        
        Optional<User> userOptional = userService.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            log.warn("존재하지 않는 이메일로 아이디 찾기 시도: {}", request.getEmail());
            throw new IllegalArgumentException("해당 이메일로 등록된 계정을 찾을 수 없습니다.");
        }
        
        User user = userOptional.get();
        emailService.sendFindIdEmail(user.getEmail(), user.getEmail());
        log.info("아이디 찾기 이메일 전송 완료: {}", user.getEmail());
    }

    /**
     * 비밀번호 재설정 요청
     * @param request 비밀번호 재설정 요청
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDto request) {
        log.debug("비밀번호 재설정 요청: 이메일={}", request.getEmail());
        
        Optional<User> userOptional = userService.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            log.warn("존재하지 않는 이메일로 비밀번호 재설정 시도: {}", request.getEmail());
            throw new IllegalArgumentException("해당 이메일로 등록된 계정을 찾을 수 없습니다.");
        }
        
        User user = userOptional.get();
        
        // 기존 토큰이 있다면 삭제
        passwordResetTokenRepository.findByUserIdAndUsedFalse(user.getId())
                .ifPresent(passwordResetTokenRepository::delete);
        
        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30)) // 30분 유효
                .used(false)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        // 이메일 전송
        emailService.sendPasswordResetEmail(user.getEmail(), token);
        log.info("비밀번호 재설정 이메일 전송 완료: {}", user.getEmail());
    }

    /**
     * 비밀번호 재설정 확인
     * @param request 비밀번호 재설정 확인 요청
     */
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmDto request) {
        log.debug("비밀번호 재설정 확인: 토큰={}", request.getToken());
        
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());
        if (tokenOptional.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        
        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 이미 사용된 토큰입니다.");
        }
        
        // 비밀번호 업데이트
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);
        
        // 토큰 사용 처리
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        log.info("비밀번호 재설정 완료: 사용자 ID={}", user.getId());
    }

    /**
     * 토큰 유효성 검증
     * @param token 토큰
     * @return 유효성 여부
     */
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        return tokenOptional.isPresent() && tokenOptional.get().isValid();
    }
}
