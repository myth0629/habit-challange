package com.habitchallenge.backend.user.controller;

import com.habitchallenge.backend.user.dto.PasswordChangeRequestDto;
import com.habitchallenge.backend.user.dto.UserProfileUpdateRequestDto;
import com.habitchallenge.backend.user.dto.UserResponseDto;
import com.habitchallenge.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                log.error("인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
            }
            
            log.debug("내 정보 조회 요청: {}", userDetails.getUsername());
            UserResponseDto userInfo = userService.getUserInfoByEmail(userDetails.getUsername());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("내 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal Server Error\",\"message\":\"서버 오류가 발생했습니다\"}");
        }
    }

    /**
     * 닉네임 변경
     */
    @PutMapping("/me/nickname")
    public ResponseEntity<?> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequestDto request) {
        try {
            if (userDetails == null) {
                log.error("인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
            }
            
            log.debug("닉네임 변경 요청: {} -> {}", userDetails.getUsername(), request.getNickname());
            UserResponseDto updatedUser = userService.updateNickname(userDetails.getUsername(), request.getNickname());
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("닉네임 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("{\"error\":\"Bad Request\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("닉네임 변경 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal Server Error\",\"message\":\"서버 오류가 발생했습니다\"}");
        }
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequestDto request) {
        try {
            if (userDetails == null) {
                log.error("인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다\"}");
            }
            
            log.debug("비밀번호 변경 요청: {}", userDetails.getUsername());
            userService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("{\"message\":\"비밀번호가 성공적으로 변경되었습니다\"}");
        } catch (IllegalArgumentException e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("{\"error\":\"Bad Request\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal Server Error\",\"message\":\"서버 오류가 발생했습니다\"}");
        }
    }
}
