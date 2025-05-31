package com.habitchallenge.backend.challenge.domain;

/**
 * 챌린지 카테고리
 * 챌린지의 분류를 나타냅니다.
 */
public enum ChallengeCategory {
    HEALTH("건강"),
    STUDY("학습"),
    HOBBY("취미"),
    SOCIAL("사회성"),
    PRODUCTIVITY("생산성"),
    MINDFULNESS("마음챙김"),
    OTHER("기타");

    private final String displayName;

    ChallengeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
