package com.habitchallenge.backend.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 전송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 비밀번호 재설정 이메일 전송
     * @param to 수신자 이메일
     * @param resetToken 비밀번호 재설정 토큰
     */
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[작심삼일 챌린지] 비밀번호 재설정");
            message.setText(
                "안녕하세요!\n\n" +
                "작심삼일 챌린지에서 비밀번호 재설정 요청을 받았습니다.\n\n" +
                "아래 링크를 클릭하여 비밀번호를 재설정해주세요:\n" +
                "http://localhost:3000/reset-password?token=" + resetToken + "\n\n" +
                "이 링크는 30분간 유효합니다.\n\n" +
                "비밀번호 재설정을 요청하지 않으셨다면 이 이메일을 무시하셔도 됩니다.\n\n" +
                "감사합니다.\n" +
                "작심삼일 챌린지 팀"
            );
            
            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 전송 완료: {}", to);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    /**
     * 아이디 찾기 이메일 전송
     * @param to 수신자 이메일
     * @param email 찾은 이메일 주소
     */
    public void sendFindIdEmail(String to, String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[작심삼일 챌린지] 아이디 찾기 결과");
            message.setText(
                "안녕하세요!\n\n" +
                "작심삼일 챌린지에서 아이디 찾기 요청을 받았습니다.\n\n" +
                "회원님의 아이디(이메일)는 다음과 같습니다:\n" +
                email + "\n\n" +
                "로그인에 문제가 있으시면 비밀번호 찾기를 이용해주세요.\n\n" +
                "감사합니다.\n" +
                "작심삼일 챌린지 팀"
            );
            
            mailSender.send(message);
            log.info("아이디 찾기 이메일 전송 완료: {}", to);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }
} 