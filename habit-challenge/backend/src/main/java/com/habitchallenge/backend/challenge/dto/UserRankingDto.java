package com.habitchallenge.backend.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 랭킹 정보 DTO
 * 사용자의 챌린지 완료 랭킹 정보를 클라이언트에 전달하기 위한 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRankingDto {
    private Long userId;
    private String userNickname;
    private String userEmail;
    private Long completedChallengesCount;
    private Long totalChallengesCount;
    private Double completionRate;
    private Integer rank;
    private String profileImageUrl;
} 