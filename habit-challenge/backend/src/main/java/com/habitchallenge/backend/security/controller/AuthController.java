package com.habitchallenge.backend.security.controller;

import com.habitchallenge.backend.auth.dto.FindIdRequestDto;
import com.habitchallenge.backend.auth.dto.PasswordResetConfirmDto;
import com.habitchallenge.backend.auth.dto.PasswordResetRequestDto;
import com.habitchallenge.backend.security.JwtUtil;
import com.habitchallenge.backend.security.dto.JwtResponse;
import com.habitchallenge.backend.security.dto.LoginRequest;
import com.habitchallenge.backend.security.service.AuthService;
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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

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
        
        UserResponseDto response = userService.register(registerRequestDto);
        log.debug("회원가입 성공: {}", registerRequestDto.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@Valid @RequestBody FindIdRequestDto request) {
        log.debug("아이디 찾기 요청 받음: {}", request.getEmail());
        
        try {
            authService.findId(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "아이디 찾기 이메일이 전송되었습니다. 이메일을 확인해주세요.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("아이디 찾기 실패: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("아이디 찾기 처리 중 오류 발생", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 비밀번호 재설정 요청
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        log.debug("비밀번호 재설정 요청 받음: {}", request.getEmail());
        
        try {
            authService.requestPasswordReset(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "비밀번호 재설정 이메일이 전송되었습니다. 이메일을 확인해주세요.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 요청 실패: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 처리 중 오류 발생", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 비밀번호 재설정 확인
     */
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDto request) {
        log.debug("비밀번호 재설정 확인 요청 받음");
        
        try {
            authService.confirmPasswordReset(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "비밀번호가 성공적으로 재설정되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 재설정 확인 실패: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("비밀번호 재설정 확인 처리 중 오류 발생", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 토큰 유효성 검증
     */
    @GetMapping("/reset-password/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        log.debug("토큰 유효성 검증 요청 받음");
        
        try {
            boolean isValid = authService.validateResetToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("토큰 유효성 검증 중 오류 발생", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
