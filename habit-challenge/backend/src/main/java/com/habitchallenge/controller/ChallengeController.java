package com.habitchallenge.controller;

import com.habitchallenge.dto.ChallengeResponse;
import com.habitchallenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 챌린지 관련 API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    /**
     * 오늘의 랜덤 챌린지를 반환합니다.
     * @return 랜덤 챌린지 정보
     */
    @GetMapping("/today")
    public ResponseEntity<ChallengeResponse> getTodayChallenge() {
        ChallengeResponse response = challengeService.getRandomChallenge();
        return ResponseEntity.ok(response);
    }
}
