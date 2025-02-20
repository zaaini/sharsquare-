package com.example.ss;

public class UploadResponse {
    private boolean success;
    private String message;
    // You can add more fields if your API response contains more data
    private String fileUrl;

    // Constructor
    public UploadResponse(boolean success, String message, String fileUrl) {
        this.success = success;
        this.message = message;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
