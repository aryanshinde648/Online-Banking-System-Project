package com.obs.Online_Banking_System.exception;

public class CustomerAlreadyExistsException extends RuntimeException{
    public CustomerAlreadyExistsException(String msg){
        super(msg);
    }
}
