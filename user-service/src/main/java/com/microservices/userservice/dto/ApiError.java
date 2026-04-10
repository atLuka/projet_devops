package com.microservices.userservice.dto;

import java.time.LocalDateTime;

public class ApiError {

    private int status;
    private String error;
    private String message;
    private String service;
    private String timestamp;

    public ApiError(int status, String error, String message, String service) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.service = service;
        this.timestamp = LocalDateTime.now().toString();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
