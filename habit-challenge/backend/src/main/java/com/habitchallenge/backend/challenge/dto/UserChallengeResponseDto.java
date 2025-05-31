package com.habitchallenge.backend.challenge.dto;

import com.habitchallenge.backend.challenge.domain.UserChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 챌린지 응답 DTO
 * 사용자 챌린지 정보를 클라이언트에 전달하기 위한 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserChallengeResponseDto {
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
    private String status;
    private String statusDisplayName;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 사용자 챌린지 엔티티를 DTO로 변환
     * @param userChallenge 사용자 챌린지 엔티티
     * @return 사용자 챌린지 응답 DTO
     */
    public static UserChallengeResponseDto from(UserChallenge userChallenge) {
        return UserChallengeResponseDto.builder()
                .id(userChallenge.getId())
                .userId(userChallenge.getUser().getId())
                .userNickname(userChallenge.getUser().getNickname())
                .challengeId(userChallenge.getChallenge().getId())
                .challengeTitle(userChallenge.getChallenge().getTitle())
                .challengeDescription(userChallenge.getChallenge().getDescription())
                .category(userChallenge.getChallenge().getCategory().name())
                .categoryDisplayName(userChallenge.getChallenge().getCategory().getDisplayName())
                .difficulty(userChallenge.getChallenge().getDifficulty())
                .challengeDate(userChallenge.getChallengeDate())
                .status(userChallenge.getStatus().name())
                .statusDisplayName(userChallenge.getStatus().getDisplayName())
                .note(userChallenge.getNote())
                .createdAt(userChallenge.getCreatedAt())
                .updatedAt(userChallenge.getUpdatedAt())
                .build();
    }
}
