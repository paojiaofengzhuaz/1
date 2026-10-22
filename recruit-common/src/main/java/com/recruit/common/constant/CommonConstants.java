package com.recruit.common.constant;

/**
 * 公共常量
 */
public final class CommonConstants {

    /** 请求头：Token */
    public static final String TOKEN_HEADER = "Authorization";

    /** 网关向下游透传的用户ID */
    public static final String USER_ID_HEADER = "X-UserId";

    /** 网关向下游透传的角色（1求职者 2招聘者） */
    public static final String ROLE_HEADER = "X-Role";

    /** Redis 中 Token 的 key 前缀 */
    public static final String TOKEN_KEY_PREFIX = "recruit:token:";

    /** 角色：求职者 */
    public static final int ROLE_SEEKER = 1;

    /** 角色：招聘者 */
    public static final int ROLE_RECRUITER = 2;

    private CommonConstants() {
    }
}
