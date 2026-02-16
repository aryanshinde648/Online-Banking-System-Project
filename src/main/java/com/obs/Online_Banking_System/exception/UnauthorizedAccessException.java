package com.obs.Online_Banking_System.exception;

public class UnauthorizedAccessException extends RuntimeException{
    public UnauthorizedAccessException(String msg){
        super(msg);
    }
}
