package com.habitchallenge.backend.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 챌린지 완료 요청 DTO
 * 챌린지 완료 시 필요한 사진과 후기 정보를 받기 위한 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeCompletionDto {
    private String photoUrl;
    private String review;
} 