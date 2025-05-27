package com.habitchallenge.service;

import com.habitchallenge.dto.ChallengeResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 챌린지 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
public class ChallengeService {
    
    // 미리 정의된 챌린지 목록
    private final List<String> challenges = Arrays.asList(
        "하루 1L 물 마시기 💧",
        "30분 산책하기 🚶‍♂️",
        "책 10페이지 읽기 📚",
        "5분 명상하기 🧘‍♀️",
        "새로운 요리 도전하기 🍳",
        "감사 일기 쓰기 ✍️",
        "10분 스트레칭 하기 🧘",
        "스마트폰 사용 시간 1시간 줄이기 📵",
        "친구나 가족에게 감사 인사 전하기 💌",
        "하루 30분 일찍 일어나기 ⏰"
    );
    
    private final Random random = new Random();
    
    /**
     * 랜덤한 챌린지 하나를 반환합니다.
     * @return 랜덤하게 선택된 챌린지
     */
    public ChallengeResponse getRandomChallenge() {
        String randomChallenge = challenges.get(random.nextInt(challenges.size()));
        return new ChallengeResponse(randomChallenge);
    }
}
