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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto register(RegisterRequestDto requestDto) {
        // 비밀번호 확인
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(requestDto.getUsername())) {
            throw new DuplicateUsernameException("이미 사용 중인 닉네임입니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .email(requestDto.getEmail())
                .nickname(requestDto.getUsername())  // username을 nickname으로 매핑
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(UserRole.USER)
                .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);

        return UserResponseDto.from(savedUser);
    }
}
