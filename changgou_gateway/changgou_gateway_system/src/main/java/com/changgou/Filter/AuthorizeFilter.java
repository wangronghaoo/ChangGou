package com.changgou.Filter;

import io.jsonwebtoken.Jwts;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求
        ServerHttpRequest request = exchange.getRequest();
        //获取响应
        ServerHttpResponse response = exchange.getResponse();
        //如果是登录就放行
        if (request.getURI().getPath().contains("/admin/login")){
            return chain.filter(exchange);
        }
        //获取请求头
        HttpHeaders headers = request.getHeaders();
        //请求头中获取令牌
        String token = headers.getFirst("config");
        //判断请求头中是否包含令牌
        if(StringUtils.isEmpty(token)){
            //没有权限访问
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //返回
            return response.setComplete();
        }


        //如果有则解析令牌
        try {
            byte[] encodeKey = Base64.getDecoder().decode("wangronghao");
            SecretKey secretKey = new SecretKeySpec(encodeKey,0,encodeKey.length,"AES");
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            //放行
            return chain.filter(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            //解析出错
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
