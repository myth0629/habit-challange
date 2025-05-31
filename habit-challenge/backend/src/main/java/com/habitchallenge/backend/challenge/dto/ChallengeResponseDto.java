package com.habitchallenge.backend.challenge.dto;

import com.habitchallenge.backend.challenge.domain.Challenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 챌린지 응답 DTO
 * 챌린지 정보를 클라이언트에 전달하기 위한 데이터 전송 객체입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeResponseDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String categoryDisplayName;
    private Integer difficulty;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 챌린지 엔티티를 DTO로 변환
     * @param challenge 챌린지 엔티티
     * @return 챌린지 응답 DTO
     */
    public static ChallengeResponseDto from(Challenge challenge) {
        return ChallengeResponseDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .category(challenge.getCategory().name())
                .categoryDisplayName(challenge.getCategory().getDisplayName())
                .difficulty(challenge.getDifficulty())
                .isActive(challenge.getIsActive())
                .createdAt(challenge.getCreatedAt())
                .updatedAt(challenge.getUpdatedAt())
                .build();
    }
}
