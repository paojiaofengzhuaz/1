package com.recruit.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局鉴权：
 * 1. 公共接口（登录注册、岗位浏览、智能助手）直接放行
 * 2. 其余接口校验 Redis 中的 Token，并把 userId / role 注入请求头透传给下游服务
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-UserId";
    private static final String ROLE_HEADER = "X-Role";
    private static final String TOKEN_KEY_PREFIX = "recruit:token:";

    private final StringRedisTemplate redis;

    public AuthFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 公共接口白名单
        if (path.startsWith("/user/register") || path.startsWith("/user/login")) {
            return chain.filter(exchange);
        }
        if (HttpMethod.GET.equals(method) && isPublicJobPath(path)) {
            return chain.filter(exchange);
        }
        if (HttpMethod.GET.equals(method) && path.startsWith("/match/assistant")) {
            return chain.filter(exchange);
        }
        // AI 求职小助手：免登录可用；带了 Token 时仍然解析身份，便于给出个性化建议
        if (isOptionalAuthPath(path)) {
            return chain.filter(withUserIfToken(exchange));
        }

        // 校验 Token
        String token = request.getHeaders().getFirst(TOKEN_HEADER);
        if (token == null || token.trim().isEmpty()) {
            return unauthorized(exchange, "请先登录");
        }
        String value = redis.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (value == null) {
            return unauthorized(exchange, "登录已过期，请重新登录");
        }

        // value 格式：userId:role
        String[] parts = value.split(":");
        String userId = parts[0];
        String role = parts.length > 1 ? parts[1] : "1";
        ServerHttpRequest newRequest = request.mutate()
                .header(USER_ID_HEADER, userId)
                .header(ROLE_HEADER, role)
                .build();
        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    /**
     * 可选鉴权路径：不强制登录，但请求带 Token 且有效时仍然把 userId / role 透传给下游
     */
    private boolean isOptionalAuthPath(String path) {
        return path.startsWith("/match/ai/");
    }

    /** 若有有效 Token 则注入用户头，否则原样透传 */
    private ServerWebExchange withUserIfToken(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().getFirst(TOKEN_HEADER);
        if (token == null || token.trim().isEmpty()) {
            return exchange;
        }
        String value = redis.opsForValue().get(TOKEN_KEY_PREFIX + token);
        if (value == null) {
            return exchange;
        }
        String[] parts = value.split(":");
        String userId = parts[0];
        String role = parts.length > 1 ? parts[1] : "1";
        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header(USER_ID_HEADER, userId)
                .header(ROLE_HEADER, role)
                .build();
        return exchange.mutate().request(newRequest).build();
    }

    /**
     * 岗位浏览类接口免登录：列表、热门、以及 /job/{id} 详情；
     * /job/my、/job/publish、/job/offline、/job/all 仍需鉴权
     */
    private boolean isPublicJobPath(String path) {
        if ("/job/list".equals(path) || "/job/hot".equals(path)
                || "/job/stats".equals(path) || "/job/companies".equals(path)) {
            return true;
        }
        return path.matches("^/job/\\d+$");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
