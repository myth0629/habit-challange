package com.habitchallenge.backend.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 에러 응답 DTO
 * 클라이언트에게 에러 정보를 전달하기 위한 공통 응답 형식
 */
@Getter
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    private Object data;
    
    /**
     * 에러 메시지만 포함하는 생성자
     * @param message 에러 메시지
     */
    public ErrorResponse(String message) {
        this.message = message;
    }
    
    /**
     * 에러 메시지와 추가 데이터를 포함하는 생성자
     * @param message 에러 메시지
     * @param data 추가 데이터
     */
    public ErrorResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }
}
