package com.example.demo.exception;

public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException() {
        super("EMAIL_NOT_VERIFIED");
    }
}