package com.habitchallenge.backend.challenge.controller;

import com.habitchallenge.backend.challenge.domain.ChallengeStatus;
import com.habitchallenge.backend.challenge.dto.UserChallengeResponseDto;
import com.habitchallenge.backend.challenge.service.UserChallengeService;
import com.habitchallenge.backend.common.dto.ErrorResponse;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 챌린지 컨트롤러
 * 사용자 챌린지 관련 API 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/user-challenges")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserChallengeController {

    private final UserChallengeService userChallengeService;
    private final UserRepository userRepository;

    /**
     * 랜덤 챌린지 할당 API
     * @param userDetails 사용자 인증 정보
     * @return 할당된 챌린지 정보
     */
    @PostMapping("/assign-random")
    public ResponseEntity<?> assignRandomChallenge(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("랜덤 챌린지 할당 요청: 사용자={}", userDetails.getUsername());
        
        try {
            // 사용자 이메일로 사용자 정보 조회
            Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
            if (userOptional.isEmpty()) {
                log.error("사용자를 찾을 수 없습니다: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User not found"));
            }
            
            User user = userOptional.get();
            log.debug("사용자 정보: id={}, email={}", user.getId(), user.getEmail());
            
            // 오늘 날짜 기준으로 이미 할당된 챌린지가 있는지 확인
            LocalDate today = LocalDate.now();
            List<UserChallengeResponseDto> todaysChallenges = userChallengeService.getUserChallengesByDate(user.getId(), today);
            
            if (!todaysChallenges.isEmpty()) {
                log.debug("사용자 {}에게 이미 오늘({})의 챌린지가 할당되어 있습니다.", user.getId(), today);
                UserChallengeResponseDto existingChallenge = todaysChallenges.get(0);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("A challenge is already assigned for today", existingChallenge));
            }
            
            // 랜덤 챌린지 할당
            Optional<UserChallengeResponseDto> resultOptional = userChallengeService.assignRandomChallenge(user.getId());
            
            if (resultOptional.isEmpty()) {
                log.error("랜덤 챌린지 할당 실패: 사용자 ID={}", user.getId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("Failed to assign random challenge"));
            }
            
            UserChallengeResponseDto result = resultOptional.get();
            log.debug("랜덤 챌린지 할당 성공: 사용자 ID={}, 챌린지 ID={}", user.getId(), result.getChallengeId());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("랜덤 챌린지 할당 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while assigning a random challenge: " + e.getMessage()));
        }
    }

    /**
     * 사용자 챌린지 목록 조회 API
     * @param userDetails 인증된 사용자 정보
     * @return 사용자의 챌린지 목록
     */
    @GetMapping
    public ResponseEntity<List<UserChallengeResponseDto>> getUserChallenges(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("사용자 챌린지 목록 조회 API 호출");
        
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.badRequest().build();
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
            
            List<UserChallengeResponseDto> userChallenges = userChallengeService.getUserChallenges(userId);
            log.debug("사용자 챌린지 기록 조회 결과: {} 개", userChallenges.size());
            
            return ResponseEntity.ok(userChallenges);
        } catch (Exception e) {
            log.error("챌린지 기록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 날짜의 사용자 챌린지 조회 API
     * @param userDetails 인증된 사용자 정보
     * @param date 조회할 날짜
     * @return 해당 날짜의 사용자 챌린지 목록
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<UserChallengeResponseDto>> getUserChallengesByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("특정 날짜의 사용자 챌린지 조회 API 호출: {}", date);
        
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.badRequest().build();
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
            
            List<UserChallengeResponseDto> userChallenges = userChallengeService.getUserChallengesByDate(userId, date);
            log.debug("날짜 {} 의 사용자 챌린지 기록 조회 결과: {} 개", date, userChallenges.size());
            
            return ResponseEntity.ok(userChallenges);
        } catch (Exception e) {
            log.error("날짜별 챌린지 기록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 챌린지 상태 업데이트 API
     * @param userChallengeId 사용자 챌린지 ID
     * @param status 새로운 상태
     * @return 업데이트된 사용자 챌린지 정보
     */
    @PutMapping("/{userChallengeId}/status/{status}")
    public ResponseEntity<UserChallengeResponseDto> updateChallengeStatus(
            @PathVariable Long userChallengeId,
            @PathVariable String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("사용자 챌린지 상태 업데이트 API 호출: ID={}, 상태={}", userChallengeId, status);
        
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.badRequest().build();
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
            
            // 상태 변환
            ChallengeStatus challengeStatus;
            try {
                challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("잘못된 챌린지 상태: {}", status);
                return ResponseEntity.badRequest().build();
            }
            
            // 사용자 챌린지 상태 업데이트
            Optional<UserChallengeResponseDto> updatedChallenge = userChallengeService.updateChallengeStatus(userChallengeId, challengeStatus);
            
            return updatedChallenge
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("챌린지 상태 업데이트 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

/**
 * 특정 챌린지를 사용자 기록에 추가하는 API
 * @param challengeId 추가할 챌린지 ID
 * @param userDetails 인증된 사용자 정보
 * @return 추가된 챌린지 정보
 */
@PostMapping("/assign/{challengeId}")
public ResponseEntity<?> assignSpecificChallenge(
        @PathVariable Long challengeId,
        @AuthenticationPrincipal UserDetails userDetails) {
    log.debug("특정 챌린지 할당 요청: 챌린지 ID={}, 사용자={}", challengeId, userDetails.getUsername());
    
    try {
        // 사용자 이메일로 사용자 정보 조회
        Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());
        if (userOptional.isEmpty()) {
            log.error("사용자를 찾을 수 없습니다: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }
        
        User user = userOptional.get();
        log.debug("사용자 정보: id={}, email={}", user.getId(), user.getEmail());
        
        // 오늘 날짜 기준으로 이미 같은 챌린지가 할당되어 있는지 확인
        LocalDate today = LocalDate.now();
        List<UserChallengeResponseDto> todaysChallenges = userChallengeService.getUserChallengesByDate(user.getId(), today);
        
        // 이미 같은 챌린지가 할당되어 있는지 확인
        boolean alreadyAssigned = todaysChallenges.stream()
                .anyMatch(uc -> uc.getChallengeId().equals(challengeId));
        
        if (alreadyAssigned) {
            log.debug("사용자 {}에게 이미 오늘({}) 같은 챌린지 ID {}가 할당되어 있습니다.", user.getId(), today, challengeId);
            UserChallengeResponseDto existingChallenge = todaysChallenges.stream()
                    .filter(uc -> uc.getChallengeId().equals(challengeId))
                    .findFirst()
                    .get();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("This challenge is already assigned for today", existingChallenge));
        }
        
        // 특정 챌린지 할당
        Long userId = user.getId();
        Optional<UserChallengeResponseDto> resultOptional = userChallengeService.assignSpecificChallenge(userId, challengeId);
        
        if (resultOptional.isEmpty()) {
            log.error("특정 챌린지 할당 실패: 사용자 ID={}, 챌린지 ID={}", user.getId(), challengeId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to assign the specified challenge"));
        }
        
        UserChallengeResponseDto result = resultOptional.get();
        log.debug("특정 챌린지 할당 성공: 사용자 ID={}, 챌린지 ID={}", user.getId(), result.getChallengeId());
        
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        log.error("특정 챌린지 할당 중 오류 발생: {}", e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An error occurred while assigning the challenge: " + e.getMessage()));
    }
}

    /**
     * 사용자 챌린지 삭제 API
     * @param userChallengeId 삭제할 사용자 챌린지 ID
     * @param userDetails 인증된 사용자 정보
     * @return 삭제 성공 여부
     */
    @DeleteMapping("/{userChallengeId}")
    public ResponseEntity<?> deleteUserChallenge(
            @PathVariable Long userChallengeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("사용자 챌린지 삭제 API 호출: ID={}", userChallengeId);
        
        if (userDetails == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 사용자 이메일을 토큰에서 추출
            String email = userDetails.getUsername();
            
            // 이메일로 사용자 조회
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("이메일로 사용자를 찾을 수 없습니다: {}", email);
                return ResponseEntity.badRequest().build();
            }
            
            Long userId = userOptional.get().getId();
            
            // 사용자의 챌린지인지 확인 후 삭제
            boolean deleted = userChallengeService.deleteUserChallenge(userChallengeId, userId);
            
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (Exception e) {
            log.error("챌린지 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 사용자 챌린지 노트 업데이트 API
     * @param userChallengeId 사용자 챌린지 ID
     * @param note 새로운 노트
     * @return 업데이트된 사용자 챌린지 정보
     */
    @PutMapping("/{userChallengeId}/note")
    public ResponseEntity<UserChallengeResponseDto> updateChallengeNote(
            @PathVariable Long userChallengeId,
            @RequestBody String note) {
        log.debug("사용자 챌린지 노트 업데이트 API 호출: ID={}", userChallengeId);
        
        Optional<UserChallengeResponseDto> userChallenge = userChallengeService.updateChallengeNote(userChallengeId, note);
        
        return userChallenge
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
