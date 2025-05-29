package com.habitchallenge.backend.security.service;

import com.habitchallenge.backend.security.JwtUtil;
import com.habitchallenge.backend.security.dto.JwtResponse;
import com.habitchallenge.backend.security.dto.LoginRequest;
import com.habitchallenge.backend.user.domain.User;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

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
}
