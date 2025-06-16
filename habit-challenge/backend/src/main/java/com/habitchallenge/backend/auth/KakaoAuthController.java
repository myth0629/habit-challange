package com.habitchallenge.backend.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> kakaoLogin(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "인증 코드가 필요합니다."));
            }

            log.info("[카카오 로그인] 인증 코드 수신: {}", code);
            
            Map<String, Object> response = kakaoAuthService.processKakaoLogin(code);
            
            log.info("[카카오 로그인] 성공: {}", response.get("email"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[카카오 로그인] 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "카카오 로그인 처리 중 오류가 발생했습니다."));
        }
    }
} 