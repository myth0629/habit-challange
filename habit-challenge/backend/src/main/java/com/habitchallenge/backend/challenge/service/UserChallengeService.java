package com.habitchallenge.backend.challenge.service;

import com.habitchallenge.backend.challenge.domain.Challenge;
import com.habitchallenge.backend.challenge.domain.ChallengeStatus;
import com.habitchallenge.backend.challenge.domain.UserChallenge;
import com.habitchallenge.backend.challenge.dto.UserChallengeResponseDto;
import com.habitchallenge.backend.challenge.repository.ChallengeRepository;
import com.habitchallenge.backend.challenge.repository.UserChallengeRepository;
import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
}
