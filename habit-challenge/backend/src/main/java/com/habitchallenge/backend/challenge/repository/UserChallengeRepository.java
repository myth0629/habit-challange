package com.habitchallenge.backend.challenge.repository;

import com.habitchallenge.backend.challenge.domain.UserChallenge;
import com.habitchallenge.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 챌린지 레포지토리
 * 사용자 챌린지 기록에 대한 데이터 액세스를 제공합니다.
 */
@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    
    /**
     * 사용자별 모든 챌린지 기록 조회
     * @param user 사용자
     * @return 사용자의 모든 챌린지 기록
     */
    List<UserChallenge> findByUser(User user);
    
    /**
     * 사용자별 특정 날짜의 챌린지 기록 조회
     * @param user 사용자
     * @param challengeDate 챌린지 날짜
     * @return 사용자의 특정 날짜 챌린지 기록
     */
    List<UserChallenge> findByUserAndChallengeDate(User user, LocalDate challengeDate);
    
    /**
     * 사용자별 특정 날짜 범위의 챌린지 기록 조회
     * @param user 사용자
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 사용자의 특정 날짜 범위 챌린지 기록
     */
    List<UserChallenge> findByUserAndChallengeDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    /**
     * 사용자별 챌린지 기록을 최신순으로 정렬하여 조회
     * @param user 사용자
     * @return 사용자의 챌린지 기록을 최신순으로 정렬하여 조회
     */
    List<UserChallenge> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 사용자별 최근 챌린지 기록 조회
     * @param user 사용자
     * @return 사용자의 최근 챌린지 기록
     */
    Optional<UserChallenge> findFirstByUserOrderByChallengeDateDesc(User user);
}
