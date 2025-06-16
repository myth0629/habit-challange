package com.habitchallenge.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 아이디 찾기 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindIdRequestDto {
    private String email;
} 