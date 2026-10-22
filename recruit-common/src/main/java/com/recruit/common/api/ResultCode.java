package com.recruit.common.api;

/**
 * 业务状态码
 */
public enum ResultCode {

    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "系统繁忙，请稍后再试"),

    LOGIN_FAIL(1001, "用户名或密码错误"),
    USER_EXISTS(1002, "用户名已存在"),
    USER_NOT_FOUND(1003, "用户不存在"),

    JOB_NOT_FOUND(2001, "岗位不存在"),

    RESUME_NOT_FOUND(3001, "请先完善简历"),
    DELIVERY_EXISTS(3002, "你已经投递过该岗位"),

    ROLE_ERROR(4001, "当前身份无权执行该操作");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
