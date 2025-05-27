package com.habitchallenge.backend.user.repository;

import com.habitchallenge.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 정보를 데이터베이스에서 조회 및 관리하기 위한 리포지토리 인터페이스입니다.
 * Spring Data JPA를 사용하여 기본적인 CRUD 작업을 제공합니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 존재 여부를 확인합니다.
     *
     * @param email 확인할 이메일
     * @return 사용자 존재 여부
     */
    boolean existsByEmail(String email);
    
    /**
     * 닉네임으로 사용자 존재 여부를 확인합니다.
     *
     * @param nickname 확인할 사용자 닉네임
     * @return 사용자 존재 여부
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 조회할 사용자 이메일
     * @return Optional로 감싼 사용자 정보
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 닉네임으로 사용자를 조회합니다.
     *
     * @param nickname 조회할 사용자 닉네임
     * @return Optional로 감싼 사용자 정보
     */
    Optional<User> findByNickname(String nickname);
}
