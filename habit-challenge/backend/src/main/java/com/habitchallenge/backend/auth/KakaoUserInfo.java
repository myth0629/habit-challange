package com.habitchallenge.backend.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaoUserInfo {
    private Long kakaoId;
    private String nickname;
    private String email;
} 