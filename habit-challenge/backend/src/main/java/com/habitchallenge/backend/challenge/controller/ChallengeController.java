package com.habitchallenge.backend.challenge.controller;

import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import com.habitchallenge.backend.challenge.dto.ChallengeResponseDto;
import com.habitchallenge.backend.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 컨트롤러
 * 챌린지 관련 API 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChallengeController {

    private final ChallengeService challengeService;

    /**
     * 모든 챌린지 조회 API
     * @return 모든 챌린지 목록
     */
    @GetMapping("/all")
    public ResponseEntity<List<ChallengeResponseDto>> getAllChallenges() {
        log.debug("모든 챌린지 조회 API 호출");
        List<ChallengeResponseDto> challenges = challengeService.getAllChallenges();
        return ResponseEntity.ok(challenges);
    }

    /**
     * 활성화된 모든 챌린지 조회 API
     * @return 활성화된 챌린지 목록
     */
    @GetMapping
    public ResponseEntity<List<ChallengeResponseDto>> getActiveChallenges() {
        log.debug("활성화된 챌린지 조회 API 호출");
        List<ChallengeResponseDto> challenges = challengeService.getActiveChallenges();
        return ResponseEntity.ok(challenges);
    }

    /**
     * 챌린지 ID로 조회 API
     * @param id 챌린지 ID
     * @return 챌린지 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponseDto> getChallengeById(@PathVariable Long id) {
        log.debug("챌린지 ID로 조회 API 호출: {}", id);
        Optional<ChallengeResponseDto> challenge = challengeService.getChallengeById(id);
        return challenge
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 카테고리별 챌린지 조회 API
     * @param category 챌린지 카테고리
     * @return 해당 카테고리의 활성화된 챌린지 목록
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ChallengeResponseDto>> getChallengesByCategory(@PathVariable String category) {
        log.debug("카테고리별 챌린지 조회 API 호출: {}", category);
        try {
            ChallengeCategory challengeCategory = ChallengeCategory.valueOf(category.toUpperCase());
            List<ChallengeResponseDto> challenges = challengeService.getChallengesByCategory(challengeCategory);
            return ResponseEntity.ok(challenges);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 카테고리: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 난이도별 챌린지 조회 API
     * @param difficulty 챌린지 난이도
     * @return 해당 난이도의 활성화된 챌린지 목록
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<ChallengeResponseDto>> getChallengesByDifficulty(@PathVariable Integer difficulty) {
        log.debug("난이도별 챌린지 조회 API 호출: {}", difficulty);
        if (difficulty < 1 || difficulty > 5) {
            return ResponseEntity.badRequest().build();
        }
        List<ChallengeResponseDto> challenges = challengeService.getChallengesByDifficulty(difficulty);
        return ResponseEntity.ok(challenges);
    }

    /**
     * 랜덤 챌린지 조회 API
     * @return 랜덤 챌린지 정보
     */
    @GetMapping("/random")
    public ResponseEntity<ChallengeResponseDto> getRandomChallenge() {
        log.debug("랜덤 챌린지 조회 API 호출");
        Optional<ChallengeResponseDto> challenge = challengeService.getRandomChallenge();
        return challenge
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
