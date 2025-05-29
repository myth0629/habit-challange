package com.habitchallenge.backend.config;

import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.domain.UserRole;
import com.habitchallenge.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 테스트 데이터를 초기화하는 컴포넌트
 * 애플리케이션 시작 시 테스트 사용자를 생성합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 테스트 사용자 생성
        if (userRepository.count() == 0) {
            createTestUser("test@example.com", "test1234", "테스트사용자");
            createTestUser("bruno0629@naver.com", "song010629", "브루노");
            log.info("테스트 사용자가 생성되었습니다.");
        } else {
            log.info("이미 사용자가 존재합니다. 테스트 사용자 생성을 건너뜁니다.");
        }
    }

    private void createTestUser(String email, String password, String nickname) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
        log.info("테스트 사용자 생성: {}", email);
    }
}
