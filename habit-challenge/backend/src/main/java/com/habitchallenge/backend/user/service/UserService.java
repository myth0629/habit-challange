package com.habitchallenge.backend.user.service;

import com.habitchallenge.backend.user.domain.User;
import com.habitchallenge.backend.user.domain.UserRole;
import com.habitchallenge.backend.user.dto.RegisterRequestDto;
import com.habitchallenge.backend.user.dto.UserResponseDto;
import com.habitchallenge.backend.user.exception.DuplicateEmailException;
import com.habitchallenge.backend.user.exception.DuplicateUsernameException;
import com.habitchallenge.backend.user.exception.PasswordMismatchException;
import com.habitchallenge.backend.user.exception.UserNotFoundException;
import com.habitchallenge.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 이메일로 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfoByEmail(String email) {
        User user = getUserByEmail(email);
        return UserResponseDto.from(user);
    }

    @Transactional
    public UserResponseDto register(RegisterRequestDto requestDto) {
        // 비밀번호 확인
        if (!requestDto.isPasswordMatching()) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new DuplicateUsernameException("이미 사용 중인 닉네임입니다.");
        }

        try {
            // 사용자 생성
            User user = User.builder()
                    .email(requestDto.getEmail())
                    .nickname(requestDto.getNickname())
                    .password(passwordEncoder.encode(requestDto.getPassword()))
                    .role(UserRole.USER)
                    .build();

            // 사용자 저장
            User savedUser = userRepository.save(user);
            return UserResponseDto.from(savedUser);
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public UserResponseDto updateNickname(String email, String newNickname) {
        log.debug("닉네임 변경 요청: {} -> {}", email, newNickname);
        
        // 사용자 조회
        User user = getUserByEmail(email);
        
        // 새 닉네임이 기존 닉네임과 같은지 확인
        if (user.getNickname().equals(newNickname)) {
            throw new IllegalArgumentException("새 닉네임이 기존 닉네임과 같습니다.");
        }
        
        // 닉네임 중복 체크
        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 닉네임 변경
        user.updateNickname(newNickname);
        User updatedUser = userRepository.save(user);
        
        log.debug("닉네임 변경 완료: {} -> {}", email, newNickname);
        return UserResponseDto.from(updatedUser);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        log.debug("비밀번호 변경 요청: {}", email);
        
        // 사용자 조회
        User user = getUserByEmail(email);
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 현재 비밀번호와 같습니다.");
        }
        
        // 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.debug("비밀번호 변경 완료: {}", email);
    }

    /**
     * 이메일로 사용자 Optional 조회 (비밀번호/아이디 찾기 등에서 사용)
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 저장 (비밀번호 재설정 등에서 사용)
     */
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
