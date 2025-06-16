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

    @Column
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String review;

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

    /**
     * 챌린지 사진 URL 업데이트
     * @param photoUrl 새로운 사진 URL
     */
    public void updatePhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * 챌린지 후기 업데이트
     * @param review 새로운 후기
     */
    public void updateReview(String review) {
        this.review = review;
    }

    /**
     * 챌린지 완료 정보 업데이트 (사진과 후기 포함)
     * @param photoUrl 사진 URL
     * @param review 후기
     */
    public void completeChallenge(String photoUrl, String review) {
        this.photoUrl = photoUrl;
        this.review = review;
        this.status = ChallengeStatus.COMPLETED;
    }
}
