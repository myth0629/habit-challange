package com.habitchallenge.backend.security.controller;

import com.habitchallenge.backend.security.JwtUtil;
import com.habitchallenge.backend.security.dto.JwtResponse;
import com.habitchallenge.backend.security.dto.LoginRequest;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.dto.RegisterRequestDto;
import com.habitchallenge.backend.user.dto.UserResponseDto;
import com.habitchallenge.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.debug("로그인 요청 받음: {}", loginRequest.getEmail());
        
        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            log.debug("인증 성공: {}", authentication.getName());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 사용자 정보 가져오기
            User user = userService.getUserByEmail(loginRequest.getEmail());
            log.debug("사용자 정보 가져오기 성공: {}", user.getNickname());
            
            // JWT 토큰 생성
            String jwt = jwtUtil.generateToken(user);
            log.debug("JWT 토큰 생성 완료");
            
            // 응답 생성
            JwtResponse response = JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
                
            log.debug("로그인 성공: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.error("인증 실패: 잘못된 자격 증명", e);
            JwtResponse errorResponse = JwtResponse.builder()
                .errorMessage("이메일 또는 비밀번호가 일치하지 않습니다.")
                .build();
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            JwtResponse errorResponse = JwtResponse.builder()
                .errorMessage("서버 오류가 발생했습니다.")
                .build();            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.debug("회원가입 요청 받음: {}", registerRequestDto.getEmail());
        
        try {
            UserResponseDto response = userService.register(registerRequestDto);
            log.debug("회원가입 성공: {}", registerRequestDto.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            JwtResponse errorResponse = JwtResponse.builder()
                .errorMessage(e.getMessage())
                .build();
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
}
