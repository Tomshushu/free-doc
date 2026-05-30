package com.freedoc.common.exception;

import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage());
        return R.failWithArgs(e.getCode(), e.getMessage(), e.getArgs());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<R<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(R.fail(HttpStatus.FORBIDDEN.value(), "error.auth.noPermission"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("Resource not found exception: {}", e.getMessage());
        String messageKey = "error.system.resourceNotFound";

        if (e.getMessage() != null && e.getMessage().contains("export/")) {
            messageKey = "error.system.exportPathError";
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(R.fail(HttpStatus.NOT_FOUND.value(), messageKey));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation failed: {}", message);
        return R.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Binding failed: {}", message);
        return R.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("System exception: ", e);
        return R.fail("error.system.internalError");
    }

}
