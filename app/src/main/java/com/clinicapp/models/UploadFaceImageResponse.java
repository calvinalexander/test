package com.clinicapp.models;

import com.squareup.moshi.Json;

import java.util.List;

public class UploadFaceImageResponse {
    @Json(name="success")
    boolean success;
    @Json(name="message")
    String message;

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
}
