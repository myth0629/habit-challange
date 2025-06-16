package com.habitchallenge.backend.challenge.controller;

import com.habitchallenge.backend.challenge.dto.ChallengeReviewDto;
import com.habitchallenge.backend.challenge.service.UserChallengeService;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 챌린지 후기 컨트롤러
 * 챌린지 후기 조회 관련 API 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/challenge-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChallengeReviewController {

    private final UserChallengeService userChallengeService;
    private final UserRepository userRepository;

    /**
     * 모든 사용자의 챌린지 후기 조회 API
     * @return 모든 사용자의 챌린지 후기 목록
     */
    @GetMapping
    public ResponseEntity<List<ChallengeReviewDto>> getAllChallengeReviews() {
        log.debug("모든 사용자의 챌린지 후기 조회 API 호출");
        
        try {
            List<ChallengeReviewDto> reviews = userChallengeService.getAllChallengeReviews();
            log.debug("챌린지 후기 조회 결과: {} 개", reviews.size());
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("챌린지 후기 조회 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 현재 사용자의 챌린지 후기 조회 API
     * @param userDetails 인증된 사용자 정보
     * @return 현재 사용자의 챌린지 후기 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<ChallengeReviewDto>> getMyChallengeReviews(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("현재 사용자의 챌린지 후기 조회 API 호출");
        
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }
        
        try {
            // 사용자 이메일을 토큰에서 추출
            String email = userDetails.getUsername();
            log.debug("사용자 이메일: {}", email);
            
            // 이메일로 사용자 조회
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("이메일로 사용자를 찾을 수 없습니다: {}", email);
                return ResponseEntity.badRequest().build();
            }
            
            Long userId = userOptional.get().getId();
            log.debug("사용자 ID: {}", userId);
            
            List<ChallengeReviewDto> reviews = userChallengeService.getUserChallengeReviews(userId);
            log.debug("사용자 챌린지 후기 조회 결과: {} 개", reviews.size());
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("사용자 챌린지 후기 조회 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 사용자의 챌린지 후기 조회 API
     * @param userId 사용자 ID
     * @return 해당 사용자의 챌린지 후기 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChallengeReviewDto>> getUserChallengeReviews(@PathVariable Long userId) {
        log.debug("사용자 ID {}의 챌린지 후기 조회 API 호출", userId);
        
        try {
            List<ChallengeReviewDto> reviews = userChallengeService.getUserChallengeReviews(userId);
            log.debug("사용자 챌린지 후기 조회 결과: {} 개", reviews.size());
            
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("사용자 챌린지 후기 조회 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 