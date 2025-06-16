package com.habitchallenge.backend.challenge.service;

import com.habitchallenge.backend.challenge.domain.Challenge;
import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import com.habitchallenge.backend.challenge.domain.ChallengeStatus;
import com.habitchallenge.backend.challenge.domain.UserChallenge;
import com.habitchallenge.backend.challenge.dto.UserChallengeResponseDto;
import com.habitchallenge.backend.challenge.dto.UserRankingDto;
import com.habitchallenge.backend.challenge.dto.ChallengeCompletionDto;
import com.habitchallenge.backend.challenge.dto.ChallengeReviewDto;
import com.habitchallenge.backend.challenge.repository.ChallengeRepository;
import com.habitchallenge.backend.challenge.repository.UserChallengeRepository;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 챌린지 서비스
 * 사용자 챌린지 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final ChallengeService challengeService;

    /**
     * 사용자에게 랜덤 챌린지 할당
     * @param userId 사용자 ID
     * @return 할당된 사용자 챌린지 정보
     */
    @Transactional
    public Optional<UserChallengeResponseDto> assignRandomChallenge(Long userId) {
        log.debug("사용자 ID {}에게 랜덤 챌린지 할당", userId);
        
        // 사용자 조회
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("사용자를 찾을 수 없습니다. ID: {}", userId);
            return Optional.empty();
        }
        User user = userOptional.get();
        
        // 오늘 날짜 기준으로 이미 할당된 챌린지가 있는지 확인
        LocalDate today = LocalDate.now();
        List<UserChallenge> todaysChallenges = userChallengeRepository.findByUserAndChallengeDate(user, today);
        
        if (!todaysChallenges.isEmpty()) {
            log.debug("사용자 {}에게 이미 오늘({})의 챌린지가 할당되어 있습니다.", userId, today);
            return Optional.of(UserChallengeResponseDto.from(todaysChallenges.get(0)));
        }
        
        // 사용자가 최근에 받은 챌린지 ID 목록 조회 (최근 5개)
        List<Long> recentChallengeIds = userChallengeRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .limit(5)
                .map(uc -> uc.getChallenge().getId())
                .collect(Collectors.toList());
        
        log.debug("최근 받은 챌린지 ID: {}", recentChallengeIds);
        
        // 랜덤 챌린지 조회 (최근에 받지 않은 챌린지 우선)
        return challengeService.getRandomChallengeExcluding(recentChallengeIds)
                .flatMap(challengeDto -> {
                    Optional<Challenge> challengeOptional = challengeRepository.findById(challengeDto.getId());
                    if (challengeOptional.isEmpty()) {
                        log.error("챌린지를 찾을 수 없습니다. ID: {}", challengeDto.getId());
                        return Optional.empty();
                    }
                    
                    Challenge challenge = challengeOptional.get();
                    log.debug("사용자 {}에게 할당할 챌린지: {} - {}", userId, challenge.getId(), challenge.getTitle());
                    
                    // 사용자 챌린지 생성 및 저장
                    UserChallenge userChallenge = UserChallenge.builder()
                            .user(user)
                            .challenge(challenge)
                            .challengeDate(today)
                            .status(ChallengeStatus.ASSIGNED)
                            .build();
                    
                    UserChallenge savedUserChallenge = userChallengeRepository.save(userChallenge);
                    log.debug("사용자 {}에게 챌린지 {} 할당 완료", userId, challenge.getId());
                    
                    return Optional.of(UserChallengeResponseDto.from(savedUserChallenge));
                });
    }
    
    /**
     * 사용자에게 특정 챌린지 할당
     * @param userId 사용자 ID
     * @param challengeId 할당할 챌린지 ID
     * @return 할당된 사용자 챌린지 정보
     */
    @Transactional
    public Optional<UserChallengeResponseDto> assignSpecificChallenge(Long userId, Long challengeId) {
        log.debug("사용자 ID {}에게 특정 챌린지 {} 할당", userId, challengeId);
        
        try {
            // 사용자 조회
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                log.error("사용자를 찾을 수 없습니다. ID: {}", userId);
                return Optional.empty();
            }
            User user = userOptional.get();
            
            // 챌린지 조회
            Optional<Challenge> challengeOptional = challengeRepository.findById(challengeId);
            if (challengeOptional.isEmpty()) {
                log.error("챌린지를 찾을 수 없습니다. ID: {}", challengeId);
                return Optional.empty();
            }
            Challenge challenge = challengeOptional.get();
            
            // 오늘 날짜 기준으로 이미 할당된 챌린지가 있는지 확인
            LocalDate today = LocalDate.now();
            List<UserChallenge> todaysChallenges = userChallengeRepository.findByUserAndChallengeDate(user, today);
            
            // 이미 할당된 챌린지가 있는지 확인
            boolean alreadyAssigned = todaysChallenges.stream()
                    .anyMatch(uc -> uc.getChallenge().getId().equals(challengeId));
            
            if (alreadyAssigned) {
                log.debug("사용자 {}에게 이미 오늘({}) 같은 챌린지 ID {}가 할당되어 있습니다.", userId, today, challengeId);
                return Optional.of(UserChallengeResponseDto.from(
                    todaysChallenges.stream()
                        .filter(uc -> uc.getChallenge().getId().equals(challengeId))
                        .findFirst()
                        .get()));
            }
            
            log.debug("사용자 {}에게 할당할 챌린지: {} - {}", userId, challenge.getId(), challenge.getTitle());
            
            // 사용자 챌린지 생성 및 저장
            UserChallenge userChallenge = UserChallenge.builder()
                    .user(user)
                    .challenge(challenge)
                    .challengeDate(today)
                    .status(ChallengeStatus.ASSIGNED)
                    .build();
            
            UserChallenge savedUserChallenge = userChallengeRepository.save(userChallenge);
            log.debug("사용자 {}에게 챌린지 {} 할당 완료", userId, challenge.getId());
            
            return Optional.of(UserChallengeResponseDto.from(savedUserChallenge));
        } catch (Exception e) {
            log.error("특정 챌린지 할당 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * 사용자의 모든 챌린지 기록 조회
     * @param userId 사용자 ID
     * @return 사용자의 모든 챌린지 기록
     */
    @Transactional(readOnly = true)
    public List<UserChallengeResponseDto> getUserChallenges(Long userId) {
        log.debug("사용자 ID {}의 모든 챌린지 기록 조회", userId);
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("사용자를 찾을 수 없습니다. ID: {}", userId);
            return List.of();
        }
        
        List<UserChallenge> userChallenges = userChallengeRepository.findByUser(userOptional.get());
        return userChallenges.stream()
                .map(UserChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 날짜 챌린지 기록 조회
     * @param userId 사용자 ID  
     * @param date 날짜
     * @return 사용자의 특정 날짜 챌린지 기록
     */
    @Transactional(readOnly = true)
    public List<UserChallengeResponseDto> getUserChallengesByDate(Long userId, LocalDate date) {
        log.debug("사용자 ID {}의 날짜 {} 챌린지 기록 조회", userId, date);
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("사용자를 찾을 수 없습니다. ID: {}", userId);
            return List.of();
        }
        
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserAndChallengeDate(userOptional.get(), date);
        return userChallenges.stream()
                .map(UserChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 챌린지 상태 업데이트
     * @param userChallengeId 사용자 챌린지 ID
     * @param status 새로운 상태
     * @return 업데이트된 사용자 챌린지 정보
     */
    @Transactional
    public Optional<UserChallengeResponseDto> updateChallengeStatus(Long userChallengeId, ChallengeStatus status) {
        log.debug("사용자 챌린지 상태 업데이트: ID={}, 상태={}", userChallengeId, status);
        
        try {
            Optional<UserChallenge> userChallengeOptional = userChallengeRepository.findById(userChallengeId);
            if (userChallengeOptional.isEmpty()) {
                log.error("사용자 챌린지를 찾을 수 없습니다. ID: {}", userChallengeId);
                return Optional.empty();
            }
            
            UserChallenge userChallenge = userChallengeOptional.get();
            
            // 상태 변경 전 기존 상태 로깅
            log.debug("기존 상태: {}, 변경할 상태: {}", userChallenge.getStatus(), status);
            
            // 상태 변경
            userChallenge.setStatus(status);
            
            // 저장
            UserChallenge updatedUserChallenge = userChallengeRepository.save(userChallenge);
            log.debug("사용자 챌린지 상태 업데이트 완료: ID={}, 상태={}", userChallengeId, status);
            
            // DTO 변환 및 반환
            return Optional.of(UserChallengeResponseDto.from(updatedUserChallenge));
        } catch (Exception e) {
            log.error("사용자 챌린지 상태 업데이트 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * 사용자 챌린지 삭제
     * @param userChallengeId 삭제할 사용자 챌린지 ID
     * @param userId 사용자 ID (권한 검증용)
     * @return 삭제 성공 여부
     */
    @Transactional
    public boolean deleteUserChallenge(Long userChallengeId, Long userId) {
        log.debug("사용자 챌린지 삭제: ID={}, 사용자 ID={}", userChallengeId, userId);
        
        try {
            // 사용자 조회
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                log.error("사용자를 찾을 수 없습니다. ID: {}", userId);
                return false;
            }
            
            // 사용자 챌린지 조회
            Optional<UserChallenge> userChallengeOptional = userChallengeRepository.findById(userChallengeId);
            if (userChallengeOptional.isEmpty()) {
                log.error("사용자 챌린지를 찾을 수 없습니다. ID: {}", userChallengeId);
                return false;
            }
            
            UserChallenge userChallenge = userChallengeOptional.get();
            
            // 권한 검증: 해당 사용자의 챌린지인지 확인
            if (!userChallenge.getUser().getId().equals(userId)) {
                log.error("권한 없음: 사용자 ID={}는 챌린지 ID={}의 소유자가 아닙니다", userId, userChallengeId);
                return false;
            }
            
            // 챌린지 삭제
            userChallengeRepository.delete(userChallenge);
            log.debug("사용자 챌린지 삭제 완료: ID={}", userChallengeId);
            
            return true;
        } catch (Exception e) {
            log.error("사용자 챌린지 삭제 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 사용자 챌린지 노트 업데이트
     * @param userChallengeId 사용자 챌린지 ID
     * @param note 새로운 노트
     * @return 업데이트된 사용자 챌린지 정보
     */
    @Transactional
    public Optional<UserChallengeResponseDto> updateChallengeNote(Long userChallengeId, String note) {
        log.debug("사용자 챌린지 ID {}의 노트 업데이트", userChallengeId);
        
        Optional<UserChallenge> userChallengeOptional = userChallengeRepository.findById(userChallengeId);
        if (userChallengeOptional.isEmpty()) {
            log.error("사용자 챌린지를 찾을 수 없습니다. ID: {}", userChallengeId);
            return Optional.empty();
        }
        
        UserChallenge userChallenge = userChallengeOptional.get();
        userChallenge.updateNote(note);
        
        UserChallenge updatedUserChallenge = userChallengeRepository.save(userChallenge);
        return Optional.of(UserChallengeResponseDto.from(updatedUserChallenge));
    }

    /**
     * 챌린지 완료 (사진과 후기 포함)
     * @param userChallengeId 사용자 챌린지 ID
     * @param completionDto 챌린지 완료 정보 (사진 URL, 후기)
     * @return 완료된 사용자 챌린지 정보
     */
    @Transactional
    public Optional<UserChallengeResponseDto> completeChallenge(Long userChallengeId, ChallengeCompletionDto completionDto) {
        log.debug("챌린지 완료: ID={}, 사진={}, 후기={}", userChallengeId, completionDto.getPhotoUrl(), completionDto.getReview());
        
        try {
            Optional<UserChallenge> userChallengeOptional = userChallengeRepository.findById(userChallengeId);
            if (userChallengeOptional.isEmpty()) {
                log.error("사용자 챌린지를 찾을 수 없습니다. ID: {}", userChallengeId);
                return Optional.empty();
            }
            
            UserChallenge userChallenge = userChallengeOptional.get();
            
            // 사진과 후기가 모두 있는지 확인
            if (completionDto.getPhotoUrl() == null || completionDto.getPhotoUrl().trim().isEmpty()) {
                log.error("챌린지 완료를 위해서는 사진이 필요합니다. ID: {}", userChallengeId);
                return Optional.empty();
            }
            
            if (completionDto.getReview() == null || completionDto.getReview().trim().isEmpty()) {
                log.error("챌린지 완료를 위해서는 후기가 필요합니다. ID: {}", userChallengeId);
                return Optional.empty();
            }
            
            // 챌린지 완료 처리
            userChallenge.completeChallenge(completionDto.getPhotoUrl(), completionDto.getReview());
            
            // 저장
            UserChallenge completedUserChallenge = userChallengeRepository.save(userChallenge);
            log.debug("챌린지 완료 처리 완료: ID={}", userChallengeId);
            
            // DTO 변환 및 반환
            return Optional.of(UserChallengeResponseDto.from(completedUserChallenge));
        } catch (Exception e) {
            log.error("챌린지 완료 처리 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * 사용자의 특정 카테고리 챌린지 기록 조회
     * @param userId 사용자 ID
     * @param category 챌린지 카테고리
     * @return 해당 카테고리의 챌린지 기록 목록
     */
    @Transactional(readOnly = true)
    public List<UserChallengeResponseDto> getUserChallengesByCategory(Long userId, ChallengeCategory category) {
        log.debug("사용자 ID {}의 카테고리 {} 챌린지 기록 조회", userId, category);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        return userChallengeRepository.findByUserAndChallenge_CategoryOrderByCreatedAtDesc(user, category).stream()
                .map(UserChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 사용자의 챌린지 완료 랭킹 조회
     * @return 사용자 랭킹 목록
     */
    @Transactional(readOnly = true)
    public List<UserRankingDto> getUserRankings() {
        log.debug("사용자 챌린지 완료 랭킹 조회");
        
        List<Object[]> stats = userChallengeRepository.findUserChallengeStatsOrderByCompletedCount();
        List<UserRankingDto> rankings = new ArrayList<>();
        
        for (int i = 0; i < stats.size(); i++) {
            Object[] stat = stats.get(i);
            Long userId = (Long) stat[0];
            String userNickname = (String) stat[1];
            String userEmail = (String) stat[2];
            Long completedCount = (Long) stat[3];
            Long totalCount = (Long) stat[4];
            
            // 완료율 계산
            double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
            
            UserRankingDto ranking = UserRankingDto.builder()
                    .userId(userId)
                    .userNickname(userNickname)
                    .userEmail(userEmail)
                    .completedChallengesCount(completedCount)
                    .totalChallengesCount(totalCount)
                    .completionRate(Math.round(completionRate * 100.0) / 100.0) // 소수점 2자리까지
                    .rank(i + 1)
                    .profileImageUrl(null) // 프로필 이미지 URL은 추후 추가
                    .build();
            
            rankings.add(ranking);
        }
        
        log.debug("랭킹 조회 완료: {} 명의 사용자", rankings.size());
        return rankings;
    }

    /**
     * 특정 사용자의 랭킹 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 랭킹 정보
     */
    @Transactional(readOnly = true)
    public Optional<UserRankingDto> getUserRanking(Long userId) {
        log.debug("사용자 ID {}의 랭킹 정보 조회", userId);
        
        List<UserRankingDto> allRankings = getUserRankings();
        return allRankings.stream()
                .filter(ranking -> ranking.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * 모든 사용자의 챌린지 후기 조회 (최신순)
     * @return 모든 사용자의 챌린지 후기 목록
     */
    @Transactional(readOnly = true)
    public List<ChallengeReviewDto> getAllChallengeReviews() {
        log.debug("모든 사용자의 챌린지 후기 조회");
        
        List<UserChallenge> completedChallenges = userChallengeRepository
                .findByStatusAndReviewIsNotNullAndPhotoUrlIsNotNullOrderByCreatedAtDesc(ChallengeStatus.COMPLETED);
        
        return completedChallenges.stream()
                .map(this::convertToChallengeReviewDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 챌린지 후기 조회 (최신순)
     * @param userId 사용자 ID
     * @return 해당 사용자의 챌린지 후기 목록
     */
    @Transactional(readOnly = true)
    public List<ChallengeReviewDto> getUserChallengeReviews(Long userId) {
        log.debug("사용자 ID {}의 챌린지 후기 조회", userId);
        
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("사용자를 찾을 수 없습니다. ID: {}", userId);
            return List.of();
        }
        
        List<UserChallenge> completedChallenges = userChallengeRepository
                .findByUserAndStatusAndReviewIsNotNullOrderByCreatedAtDesc(userOptional.get(), ChallengeStatus.COMPLETED);
        
        return completedChallenges.stream()
                .map(this::convertToChallengeReviewDto)
                .collect(Collectors.toList());
    }

    /**
     * UserChallenge를 ChallengeReviewDto로 변환
     * @param userChallenge 사용자 챌린지 엔티티
     * @return 챌린지 후기 DTO
     */
    private ChallengeReviewDto convertToChallengeReviewDto(UserChallenge userChallenge) {
        return ChallengeReviewDto.builder()
                .id(userChallenge.getId())
                .userId(userChallenge.getUser().getId())
                .userNickname(userChallenge.getUser().getNickname())
                .challengeId(userChallenge.getChallenge().getId())
                .challengeTitle(userChallenge.getChallenge().getTitle())
                .challengeDescription(userChallenge.getChallenge().getDescription())
                .category(userChallenge.getChallenge().getCategory().name())
                .categoryDisplayName(userChallenge.getChallenge().getCategory().getDisplayName())
                .difficulty(userChallenge.getChallenge().getDifficulty())
                .challengeDate(userChallenge.getChallengeDate())
                .photoUrl(userChallenge.getPhotoUrl())
                .review(userChallenge.getReview())
                .createdAt(userChallenge.getCreatedAt())
                .build();
    }
}
