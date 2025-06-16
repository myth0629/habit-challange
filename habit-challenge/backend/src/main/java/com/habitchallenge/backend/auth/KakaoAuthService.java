package com.habitchallenge.backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.domain.UserRole;
import com.habitchallenge.backend.user.repository.UserRepository;
import com.habitchallenge.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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
        try {
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

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 실패", e);
            throw new RuntimeException("카카오 인증에 실패했습니다.", e);
        }
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

            JsonNode jsonNode = objectMapper.readTree(response);
            
            Long kakaoId = jsonNode.get("id").asLong();
            JsonNode properties = jsonNode.get("properties");
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            
            String nickname = properties.get("nickname").asText();
            String email = null;
            
            if (kakaoAccount.has("email") && kakaoAccount.get("email_needs_agreement").asBoolean() == false) {
                email = kakaoAccount.get("email").asText();
            }

            return KakaoUserInfo.builder()
                    .kakaoId(kakaoId)
                    .nickname(nickname)
                    .email(email)
                    .build();
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
            
            // 4. JWT 토큰 생성 (JwtUtil의 generateToken 메서드 사용)
            String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), "", java.util.Collections.emptyList()));
            
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
        // 이메일로 기존 사용자 찾기
        Optional<User> existingUser = userRepository.findByEmail(kakaoUserInfo.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 카카오 ID가 없으면 업데이트
            if (user.getKakaoId() == null) {
                user.setKakaoId(kakaoUserInfo.getKakaoId());
                userRepository.save(user);
            }
            return user;
        }

        // 새 사용자 생성
        User newUser = User.builder()
                .email(kakaoUserInfo.getEmail())
                .nickname(kakaoUserInfo.getNickname())
                .kakaoId(kakaoUserInfo.getKakaoId())
                .role(UserRole.USER)
                .password("KAKAO_USER_TEMP_PASSWORD") // 카카오 사용자를 위한 임시 비밀번호
                .build();
        
        return userRepository.save(newUser);
    }
} 