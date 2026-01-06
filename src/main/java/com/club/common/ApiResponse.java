package com.club.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;       //返还代码
    private String message; //返回信息
    private T data;         //数据
    private long timestamp; //时间戳

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    // 通用错误码
    public static final int SUCCESS = 200;      //正确响应
    public static final int BAD_REQUEST = 400;  //参数错误
    public static final int UNAUTHORIZED = 401; //未授权
    public static final int FORBIDDEN = 403;    //拒绝访问
    public static final int NOT_FOUND = 404;    //not found
    public static final int INTERNAL_SERVER_ERROR = 500;    //服务器错误
}