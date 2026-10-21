package com.whenmo.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理 DTO 欄位驗證失敗 (HTTP 400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "欄位驗證失敗", errors);
    }

    /**
     * 處理非法參數 (HTTP 400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "請求參數錯誤";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, msg, null);
    }

    /**
     * 處理查無資料 (HTTP 404)
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "查無資料";
        return buildErrorResponse(HttpStatus.NOT_FOUND, msg, null);
    }

    /**
     * 處理不存在的路由或靜態資源 (HTTP 404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(NoResourceFoundException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "找不到請求資源";
        return buildErrorResponse(HttpStatus.NOT_FOUND, msg, null);
    }

    /**
     * 處理資源狀態衝突 (HTTP 409)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "資源狀態衝突";
        return buildErrorResponse(HttpStatus.CONFLICT, msg, null);
    }

    /**
     * 處理其他未被捕捉的 RuntimeException (HTTP 500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系統執行錯誤: " + ex.getMessage(), null);
    }

    /**
     * 處理全域未預期例外 (HTTP 500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "未預期的錯誤: " + ex.getMessage(), null);
    }

    /**
     * 統一建構標準 JSON 錯誤回應格式
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, Object errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (errors != null) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}