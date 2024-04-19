package edu.hubu.controller.exception;

import edu.hubu.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ValidationController {
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException validationException){
        log.warn("Revolve [{}:{}]",validationException.getClass().getName(),validationException.getMessage());
        return RestBean.failure(400,"请求参数有误");
    }
}
