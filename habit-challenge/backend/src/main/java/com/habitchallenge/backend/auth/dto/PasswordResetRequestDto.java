package com.habitchallenge.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {
    private String email;
} 