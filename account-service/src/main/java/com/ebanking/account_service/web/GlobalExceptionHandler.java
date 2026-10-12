
package com.ebanking.account_service.web;

import com.ebanking.account_service.exceptions.*;
import com.ebanking.account_service.exceptions.BankingException;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AccountNotFoundException.class)
        public ProblemDetail handleAccountNotFound(
                        AccountNotFoundException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.NOT_FOUND,
                                ex.getMessage());
        }

        @ExceptionHandler(InvalidCustomerException.class)
        public ProblemDetail handleInvalidCustomer(
                        InvalidCustomerException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                ex.getMessage());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail handleValidation(
                        MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": "
                                                + error.getDefaultMessage())
                                .findFirst()
                                .orElse("Invalid request");

                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                message);
        }

        @ExceptionHandler(BankingException.class)
        public ProblemDetail handleBankingException(
                        BankingException ex) {
                return ProblemDetail.forStatusAndDetail(
                                ex.getStatus(),
                                ex.getMessage());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ProblemDetail handleDataConflict(
                        DataIntegrityViolationException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.CONFLICT,
                                "Operation conflicts with existing data");
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ProblemDetail handleInvalidParameter(
                        MethodArgumentTypeMismatchException ex) {
                return ProblemDetail.forStatusAndDetail(
                                HttpStatus.BAD_REQUEST,
                                "Invalid request parameter: " + ex.getName());
        }
}
