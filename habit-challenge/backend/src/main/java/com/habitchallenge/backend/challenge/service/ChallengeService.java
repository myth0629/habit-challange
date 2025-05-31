package com.habitchallenge.backend.challenge.service;

import com.habitchallenge.backend.challenge.domain.Challenge;
import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import com.habitchallenge.backend.challenge.dto.ChallengeResponseDto;
import com.habitchallenge.backend.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 챌린지 서비스
 * 챌린지 관련 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    /**
     * 모든 챌린지 조회
     * @return 모든 챌린지 목록
     */
    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> getAllChallenges() {
        log.debug("모든 챌린지 조회");
        List<Challenge> challenges = challengeRepository.findAll();
        return challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성화된 모든 챌린지 조회
     * @return 활성화된 챌린지 목록
     */
    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> getActiveChallenges() {
        log.debug("활성화된 챌린지 조회");
        List<Challenge> challenges = challengeRepository.findByIsActiveTrue();
        return challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 챌린지 ID로 조회
     * @param id 챌린지 ID
     * @return 챌린지 정보
     */
    @Transactional(readOnly = true)
    public Optional<ChallengeResponseDto> getChallengeById(Long id) {
        log.debug("챌린지 ID로 조회: {}", id);
        return challengeRepository.findById(id)
                .map(ChallengeResponseDto::from);
    }

    /**
     * 카테고리별 활성화된 챌린지 조회
     * @param category 챌린지 카테고리
     * @return 해당 카테고리의 활성화된 챌린지 목록
     */
    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> getChallengesByCategory(ChallengeCategory category) {
        log.debug("카테고리별 챌린지 조회: {}", category);
        List<Challenge> challenges = challengeRepository.findByCategoryAndIsActiveTrue(category);
        return challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 난이도별 활성화된 챌린지 조회
     * @param difficulty 챌린지 난이도
     * @return 해당 난이도의 활성화된 챌린지 목록
     */
    @Transactional(readOnly = true)
    public List<ChallengeResponseDto> getChallengesByDifficulty(Integer difficulty) {
        log.debug("난이도별 챌린지 조회: {}", difficulty);
        List<Challenge> challenges = challengeRepository.findByDifficultyAndIsActiveTrue(difficulty);
        return challenges.stream()
                .map(ChallengeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 랜덤 챌린지 조회
     * @return 랜덤하게 선택된 챌린지
     */
    public Optional<ChallengeResponseDto> getRandomChallenge() {
        List<Challenge> challenges = challengeRepository.findAll();
        
        if (challenges.isEmpty()) {
            return Optional.empty();
        }
        
        Random random = new Random();
        Challenge randomChallenge = challenges.get(random.nextInt(challenges.size()));
        
        return Optional.of(ChallengeResponseDto.from(randomChallenge));
    }
    
    /**
     * 특정 ID를 제외한 랜덤 챌린지 조회
     * @param excludeIds 제외할 챌린지 ID 목록
     * @return 랜덤하게 선택된 챌린지 (제외 목록에 없는 챌린지 우선)
     */
    public Optional<ChallengeResponseDto> getRandomChallengeExcluding(List<Long> excludeIds) {
        List<Challenge> allChallenges = challengeRepository.findAll();
        
        if (allChallenges.isEmpty()) {
            return Optional.empty();
        }
        
        // 제외할 ID 목록이 비어있으면 일반 랜덤 챌린지 반환
        if (excludeIds == null || excludeIds.isEmpty()) {
            return getRandomChallenge();
        }
        
        // 제외 목록에 없는 챌린지만 필터링
        List<Challenge> filteredChallenges = allChallenges.stream()
                .filter(challenge -> !excludeIds.contains(challenge.getId()))
                .collect(Collectors.toList());
        
        // 필터링된 챌린지가 없으면 전체 리스트에서 랜덤 선택
        if (filteredChallenges.isEmpty()) {
            Random random = new Random();
            Challenge randomChallenge = allChallenges.get(random.nextInt(allChallenges.size()));
            return Optional.of(ChallengeResponseDto.from(randomChallenge));
        }
        
        // 필터링된 챌린지 중 랜덤 선택
        Random random = new Random();
        Challenge randomChallenge = filteredChallenges.get(random.nextInt(filteredChallenges.size()));
        
        return Optional.of(ChallengeResponseDto.from(randomChallenge));
    }
}
