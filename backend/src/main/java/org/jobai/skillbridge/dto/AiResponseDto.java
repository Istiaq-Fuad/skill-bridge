package org.jobai.skillbridge.dto;

public class AiResponseDto {
    private String content;
    private String format;
    private boolean success;
    private String message;
    private long processingTimeMs;

    public AiResponseDto() {}

    public AiResponseDto(String content, String format, boolean success, String message, long processingTimeMs) {
        this.content = content;
        this.format = format;
        this.success = success;
        this.message = message;
        this.processingTimeMs = processingTimeMs;
    }

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

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

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}