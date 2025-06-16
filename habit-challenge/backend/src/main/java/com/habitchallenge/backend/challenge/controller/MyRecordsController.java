package com.habitchallenge.backend.challenge.controller;

import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import com.habitchallenge.backend.challenge.dto.UserChallengeResponseDto;
import com.habitchallenge.backend.challenge.service.UserChallengeService;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-records")
public class MyRecordsController {

    private final UserChallengeService userChallengeService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getMyRecords(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                log.error("인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
            }
            
            log.debug("인증된 사용자: {}", userDetails.getUsername());
            User user = userService.getUserByEmail(userDetails.getUsername());
            List<UserChallengeResponseDto> records = userChallengeService.getUserChallenges(user.getId());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("사용자 기록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal Server Error\",\"message\":\"서버 오류가 발생했습니다\"}");
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getMyRecordsByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String category) {
        try {
            if (userDetails == null) {
                log.error("인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
            }
            
            log.debug("인증된 사용자: {}, 카테고리: {}", userDetails.getUsername(), category);
            User user = userService.getUserByEmail(userDetails.getUsername());
            ChallengeCategory challengeCategory = ChallengeCategory.valueOf(category.toUpperCase());
            List<UserChallengeResponseDto> records = userChallengeService.getUserChallengesByCategory(user.getId(), challengeCategory);
            return ResponseEntity.ok(records);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 카테고리: {}", category);
            return ResponseEntity.badRequest()
                .body("{\"error\":\"Bad Request\",\"message\":\"잘못된 카테고리입니다\"}");
        } catch (Exception e) {
            log.error("카테고리별 사용자 기록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal Server Error\",\"message\":\"서버 오류가 발생했습니다\"}");
        }
    }
}
