package com.habitchallenge.backend.challenge.repository;

import com.habitchallenge.backend.challenge.domain.UserChallenge;
import com.habitchallenge.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.habitchallenge.backend.challenge.domain.ChallengeCategory;
import com.habitchallenge.backend.challenge.domain.ChallengeStatus;
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

    /**
     * 사용자별 특정 카테고리의 챌린지 기록을 최신순으로 정렬하여 조회
     * @param user 사용자
     * @param category 챌린지 카테고리
     * @return 해당 카테고리의 챌린지 기록 목록
     */
    List<UserChallenge> findByUserAndChallenge_CategoryOrderByCreatedAtDesc(User user, ChallengeCategory category);

    /**
     * 사용자별 완료된 챌린지 개수 조회
     * @param user 사용자
     * @return 완료된 챌린지 개수
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.user = :user AND uc.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") ChallengeStatus status);

    /**
     * 사용자별 전체 챌린지 개수 조회
     * @param user 사용자
     * @return 전체 챌린지 개수
     */
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * 모든 사용자의 챌린지 완료 통계 조회 (완료 개수 기준 내림차순)
     * @return 사용자별 챌린지 완료 통계 목록
     */
    @Query("SELECT uc.user.id as userId, uc.user.nickname as userNickname, uc.user.email as userEmail, " +
           "COUNT(CASE WHEN uc.status = 'COMPLETED' THEN 1 END) as completedCount, " +
           "COUNT(uc) as totalCount " +
           "FROM UserChallenge uc " +
           "GROUP BY uc.user.id, uc.user.nickname, uc.user.email " +
           "ORDER BY completedCount DESC, totalCount DESC")
    List<Object[]> findUserChallengeStatsOrderByCompletedCount();

    /**
     * 완료된 챌린지 중 후기가 있는 챌린지 조회 (최신순)
     * @return 후기가 있는 완료된 챌린지 목록
     */
    List<UserChallenge> findByStatusAndReviewIsNotNullOrderByCreatedAtDesc(ChallengeStatus status);

    /**
     * 특정 사용자의 완료된 챌린지 중 후기가 있는 챌린지 조회 (최신순)
     * @param user 사용자
     * @param status 챌린지 상태
     * @return 해당 사용자의 후기가 있는 완료된 챌린지 목록
     */
    List<UserChallenge> findByUserAndStatusAndReviewIsNotNullOrderByCreatedAtDesc(User user, ChallengeStatus status);

    /**
     * 모든 사용자의 완료된 챌린지 중 후기가 있는 챌린지 조회 (최신순)
     * @param status 챌린지 상태
     * @return 모든 사용자의 후기가 있는 완료된 챌린지 목록
     */
    List<UserChallenge> findByStatusAndReviewIsNotNullAndPhotoUrlIsNotNullOrderByCreatedAtDesc(ChallengeStatus status);
}
