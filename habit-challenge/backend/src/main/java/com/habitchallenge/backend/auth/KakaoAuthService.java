package com.habitchallenge.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.domain.UserRole;
import com.habitchallenge.backend.user.repository.UserRepository;
import com.habitchallenge.backend.security.JwtUtil;
import com.habitchallenge.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 카카오 인증 코드로 액세스 토큰을 받아옵니다.
     */
    public String getKakaoAccessToken(String code) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                log.debug("카카오 액세스 토큰 요청 시도 {}: code={}", retryCount + 1, code);
                
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("grant_type", "authorization_code");
                params.add("client_id", clientId);
                params.add("client_secret", clientSecret);
                params.add("code", code);
                params.add("redirect_uri", redirectUri);

                String response = webClient.post()
                        .uri("https://kauth.kakao.com/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .bodyValue(params)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.debug("카카오 액세스 토큰 응답: {}", response);
                
                JsonNode jsonNode = objectMapper.readTree(response);
                
                // 에러 응답 체크
                if (jsonNode.has("error")) {
                    String error = jsonNode.get("error").asText();
                    String errorDescription = jsonNode.has("error_description") ? 
                        jsonNode.get("error_description").asText() : "알 수 없는 오류";
                    
                    log.error("카카오 API 에러: {} - {}", error, errorDescription);
                    throw new RuntimeException("카카오 API 에러: " + error + " - " + errorDescription);
                }
                
                String accessToken = jsonNode.get("access_token").asText();
                log.debug("카카오 액세스 토큰 획득 성공");
                return accessToken;
                
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) {
                    retryCount++;
                    log.warn("카카오 API 호출 제한 (429) - 재시도 {}: {}", retryCount, e.getMessage());
                    
                    if (retryCount >= maxRetries) {
                        log.error("카카오 API 호출 제한으로 인한 최대 재시도 횟수 초과");
                        throw new RuntimeException("카카오 API 호출 제한에 걸렸습니다. 잠시 후 다시 시도해주세요.", e);
                    }
                    
                    // 재시도 전 대기 (지수 백오프)
                    try {
                        Thread.sleep(1000 * retryCount); // 1초, 2초, 3초 대기
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                } else {
                    log.error("카카오 API 호출 실패 (HTTP {}): {}", e.getStatusCode().value(), e.getMessage());
                    throw new RuntimeException("카카오 인증에 실패했습니다.", e);
                }
            } catch (Exception e) {
                log.error("카카오 액세스 토큰 요청 실패 (시도 {}): {}", retryCount + 1, e.getMessage());
                throw new RuntimeException("카카오 인증에 실패했습니다.", e);
            }
        }
        
        throw new RuntimeException("카카오 액세스 토큰 획득 실패");
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보를 받아옵니다.
     */
    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        try {
            String response = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("카카오 사용자 정보 응답: {}", response);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            Long kakaoId = jsonNode.get("id").asLong();
            JsonNode properties = jsonNode.get("properties");
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            
            // properties가 null인 경우 처리
            String nickname = "카카오사용자";
            if (properties != null && properties.has("nickname")) {
                nickname = properties.get("nickname").asText();
            } else {
                log.warn("카카오 사용자 properties가 null이거나 nickname이 없습니다. kakaoId: {}", kakaoId);
            }
            
            String email = null;
            if (kakaoAccount != null && kakaoAccount.has("email") && 
                kakaoAccount.has("email_needs_agreement") && 
                !kakaoAccount.get("email_needs_agreement").asBoolean()) {
                email = kakaoAccount.get("email").asText();
            } else {
                log.debug("카카오 사용자 이메일 정보가 없거나 동의하지 않았습니다. kakaoId: {}", kakaoId);
            }

            KakaoUserInfo userInfo = KakaoUserInfo.builder()
                    .kakaoId(kakaoId)
                    .nickname(nickname)
                    .email(email)
                    .build();
                    
            log.debug("파싱된 카카오 사용자 정보: {}", userInfo);
            return userInfo;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패", e);
            throw new RuntimeException("카카오 사용자 정보를 가져오는데 실패했습니다.", e);
        }
    }

    /**
     * 카카오 로그인을 처리합니다.
     */
    public Map<String, Object> processKakaoLogin(String code) {
        try {
            // 1. 액세스 토큰 받기
            String accessToken = getKakaoAccessToken(code);
            
            // 2. 사용자 정보 받기
            KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);
            
            // 3. 기존 사용자 확인 또는 새 사용자 생성
            User user = findOrCreateUser(kakaoUserInfo);
            
            // 4. JWT 토큰 생성 (UserService를 통해 UserDetails 가져오기)
            var userDetails = userService.loadUserByUsername(user.getEmail());
            String token = jwtUtil.generateToken(userDetails);
            
            // 5. 응답 데이터 생성
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", user.getId());
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("loginType", "KAKAO");
            
            return response;
        } catch (Exception e) {
            log.error("카카오 로그인 처리 실패", e);
            throw new RuntimeException("카카오 로그인에 실패했습니다.", e);
        }
    }

    /**
     * 기존 사용자를 찾거나 새 사용자를 생성합니다.
     */
    private User findOrCreateUser(KakaoUserInfo kakaoUserInfo) {
        log.debug("카카오 사용자 정보: kakaoId={}, email={}, nickname={}", 
            kakaoUserInfo.getKakaoId(), kakaoUserInfo.getEmail(), kakaoUserInfo.getNickname());
        
        // 1. 카카오 ID로 기존 사용자 찾기 (우선순위)
        Optional<User> existingUserByKakaoId = userRepository.findByKakaoId(kakaoUserInfo.getKakaoId());
        if (existingUserByKakaoId.isPresent()) {
            log.debug("카카오 ID로 기존 사용자 발견: {}", kakaoUserInfo.getKakaoId());
            return existingUserByKakaoId.get();
        }
        
        // 2. 이메일이 있는 경우 이메일로 기존 사용자 찾기
        if (kakaoUserInfo.getEmail() != null && !kakaoUserInfo.getEmail().trim().isEmpty()) {
            Optional<User> existingUserByEmail = userRepository.findByEmail(kakaoUserInfo.getEmail());
            if (existingUserByEmail.isPresent()) {
                User user = existingUserByEmail.get();
                log.debug("이메일로 기존 사용자 발견: {}", kakaoUserInfo.getEmail());
                // 카카오 ID가 없으면 업데이트
                if (user.getKakaoId() == null) {
                    user.setKakaoId(kakaoUserInfo.getKakaoId());
                    userRepository.save(user);
                    log.debug("기존 사용자에 카카오 ID 추가: {}", kakaoUserInfo.getKakaoId());
                }
                return user;
            }
        }

        // 3. 새 사용자 생성
        log.debug("새 카카오 사용자 생성: kakaoId={}", kakaoUserInfo.getKakaoId());
        
        // 이메일이 없는 경우 임시 이메일 생성
        String email = kakaoUserInfo.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = "kakao_" + kakaoUserInfo.getKakaoId() + "@kakao.user";
            log.debug("이메일이 없어서 임시 이메일 생성: {}", email);
        }
        
        User newUser = User.builder()
                .email(email)
                .nickname(kakaoUserInfo.getNickname())
                .kakaoId(kakaoUserInfo.getKakaoId())
                .role(UserRole.USER)
                .password("KAKAO_USER_TEMP_PASSWORD") // 카카오 사용자를 위한 임시 비밀번호
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.debug("새 카카오 사용자 생성 완료: userId={}", savedUser.getId());
        return savedUser;
    }
} 