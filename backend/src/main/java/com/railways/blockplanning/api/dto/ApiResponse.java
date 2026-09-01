package com.railways.blockplanning.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response envelope for all endpoints.
 * Ensures consistent JSON structure across the entire API.
 *
 * Decoupled frontend requirement: every response uses this wrapper
 * so the frontend API client layer (src/api/apiClient.js) can handle
 * success/error uniformly regardless of which endpoint is called.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String scoringMode;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, null);
    }

    public static <T> ApiResponse<T> ok(T data, String scoringMode) {
        return new ApiResponse<>(true, "OK", data, scoringMode);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }
}
