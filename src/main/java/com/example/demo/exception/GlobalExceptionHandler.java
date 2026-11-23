package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(AIParseException.class)
    public ProblemDetail handleAIParse(AIParseException ex) {
        log.warn("Invalid AI response", ex);
        return problem(HttpStatus.BAD_REQUEST, "No se pudo procesar la respuesta de IA.");
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerified(
            EmailNotVerifiedException ex) {

        return problem(HttpStatus.FORBIDDEN, "Debes verificar tu correo electrónico.");
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(
            ResponseStatusException ex
    ) {

        return problem(HttpStatus.valueOf(ex.getStatusCode().value()), "La solicitud no se pudo completar.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        return problem(HttpStatus.BAD_REQUEST, "La solicitud contiene datos no válidos.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unhandled server error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Se produjo un error interno.");
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }
}
