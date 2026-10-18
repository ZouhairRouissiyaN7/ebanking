package com.ebanking.chat_service.web;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ChatExceptionHandler {

    @ExceptionHandler({
            org.springframework.ai.retry.NonTransientAiException.class,
            org.springframework.ai.retry.TransientAiException.class
    })
    public ProblemDetail handleAiError(Exception ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The local AI model is temporarily unavailable.");
    }
}
