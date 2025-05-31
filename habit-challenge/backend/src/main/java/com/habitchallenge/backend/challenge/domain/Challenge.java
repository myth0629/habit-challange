package com.habitchallenge.backend.challenge.domain;

import com.habitchallenge.backend.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 챌린지 엔티티
 * 사용자가 도전할 수 있는 챌린지 정보를 저장합니다.
 */
@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeCategory category;

    @Column(nullable = false)
    private Integer difficulty; // 1-5 난이도

    @Column(nullable = false)
    private Boolean isActive;

    /**
     * 챌린지 활성화 상태 변경
     * @param isActive 활성화 여부
     */
    public void updateActiveStatus(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 챌린지 정보 업데이트
     * @param title 제목
     * @param description 설명
     * @param category 카테고리
     * @param difficulty 난이도
     */
    public void update(String title, String description, ChallengeCategory category, Integer difficulty) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
    }
}
