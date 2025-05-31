package com.habitchallenge.backend.challenge.domain;

import com.habitchallenge.backend.common.domain.BaseTimeEntity;
import com.habitchallenge.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 사용자 챌린지 기록 엔티티
 * 사용자가 수행한 챌린지 기록을 저장합니다.
 */
@Entity
@Table(name = "user_challenges")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false)
    private LocalDate challengeDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    @Column
    private String note;

    /**
     * 챌린지 상태 업데이트
     * @param status 새로운 상태
     */
    public void updateStatus(ChallengeStatus status) {
        this.status = status;
    }
    
    /**
     * 챌린지 상태 설정
     * @param status 새로운 상태
     */
    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    /**
     * 챌린지 노트 업데이트
     * @param note 새로운 노트
     */
    public void updateNote(String note) {
        this.note = note;
    }
}
