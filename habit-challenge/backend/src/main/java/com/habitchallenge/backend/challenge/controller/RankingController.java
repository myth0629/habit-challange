package com.habitchallenge.backend.challenge.controller;

import com.habitchallenge.backend.challenge.dto.UserRankingDto;
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
 * 랭킹 관련 API를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RankingController {

    private final UserChallengeService userChallengeService;
    private final UserRepository userRepository;

    /**
     * 모든 사용자의 챌린지 완료 랭킹 조회
     * @return 사용자 랭킹 목록
     */
    @GetMapping
    public ResponseEntity<List<UserRankingDto>> getAllRankings() {
        log.debug("전체 사용자 랭킹 조회 요청");
        
        try {
            List<UserRankingDto> rankings = userChallengeService.getUserRankings();
            log.debug("랭킹 조회 완료: {} 명의 사용자", rankings.size());
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            log.error("랭킹 조회 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 현재 로그인한 사용자의 랭킹 정보 조회
     * @param userDetails 인증된 사용자 정보
     * @return 사용자 랭킹 정보
     */
    @GetMapping("/my-ranking")
    public ResponseEntity<UserRankingDto> getMyRanking(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("내 랭킹 정보 조회 요청: 사용자={}", userDetails.getUsername());
        
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 사용자 이메일로 사용자 정보 조회
            Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
            if (userOptional.isEmpty()) {
                log.error("사용자를 찾을 수 없습니다: {}", userDetails.getUsername());
                return ResponseEntity.notFound().build();
            }
            
            User user = userOptional.get();
            Optional<UserRankingDto> rankingOptional = userChallengeService.getUserRanking(user.getId());
            
            if (rankingOptional.isEmpty()) {
                log.debug("사용자 {}의 랭킹 정보가 없습니다.", user.getId());
                return ResponseEntity.notFound().build();
            }
            
            UserRankingDto ranking = rankingOptional.get();
            log.debug("사용자 {}의 랭킹 정보 조회 완료: {}위", user.getId(), ranking.getRank());
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("내 랭킹 조회 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 사용자의 랭킹 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 랭킹 정보
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserRankingDto> getUserRanking(@PathVariable Long userId) {
        log.debug("사용자 ID {}의 랭킹 정보 조회 요청", userId);
        
        try {
            Optional<UserRankingDto> rankingOptional = userChallengeService.getUserRanking(userId);
            
            if (rankingOptional.isEmpty()) {
                log.debug("사용자 ID {}의 랭킹 정보가 없습니다.", userId);
                return ResponseEntity.notFound().build();
            }
            
            UserRankingDto ranking = rankingOptional.get();
            log.debug("사용자 ID {}의 랭킹 정보 조회 완료: {}위", userId, ranking.getRank());
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("사용자 랭킹 조회 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 