package com.habitchallenge.backend.challenge.repository;

import com.habitchallenge.backend.challenge.domain.Challenge;
import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 챌린지 레포지토리
 * 챌린지 엔티티에 대한 데이터 액세스를 제공합니다.
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    
    /**
     * 활성화된 모든 챌린지 조회
     * @return 활성화된 챌린지 목록
     */
    List<Challenge> findByIsActiveTrue();
    
    /**
     * 카테고리별 활성화된 챌린지 조회
     * @param category 챌린지 카테고리
     * @return 해당 카테고리의 활성화된 챌린지 목록
     */
    List<Challenge> findByCategoryAndIsActiveTrue(ChallengeCategory category);
    
    /**
     * 난이도별 활성화된 챌린지 조회
     * @param difficulty 챌린지 난이도
     * @return 해당 난이도의 활성화된 챌린지 목록
     */
    List<Challenge> findByDifficultyAndIsActiveTrue(Integer difficulty);
}
