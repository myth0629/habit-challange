package com.habitchallenge.backend.challenge.domain;

/**
 * 챌린지 상태
 * 사용자가 수행한 챌린지의 상태를 나타냅니다.
 */
public enum ChallengeStatus {
    ASSIGNED("할당됨"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료됨"),
    FAILED("실패");

    private final String displayName;

    ChallengeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
