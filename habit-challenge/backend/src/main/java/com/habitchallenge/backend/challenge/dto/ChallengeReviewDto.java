package com.habitchallenge.backend.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 챌린지 후기 조회 DTO
 * 챌린지 후기 정보를 클라이언트에 전달하기 위한 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeReviewDto {
    private Long id;
    private Long userId;
    private String userNickname;
    private Long challengeId;
    private String challengeTitle;
    private String challengeDescription;
    private String category;
    private String categoryDisplayName;
    private Integer difficulty;
    private LocalDate challengeDate;
    private String photoUrl;
    private String review;
    private LocalDateTime createdAt;
} 