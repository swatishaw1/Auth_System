package com.example.authBackend.exceptions;

public class ResourceNotFoundException extends  RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(){
        super("Resource not found !!");
    }
}