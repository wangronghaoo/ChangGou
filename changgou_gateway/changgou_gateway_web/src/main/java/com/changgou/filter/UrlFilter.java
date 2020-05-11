package com.changgou.filter;

import com.changgou.service.AuthorService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
public class UrlFilter implements GlobalFilter , Ordered {

    private static final String Authorization = "Authorization";

    //登录路径
    private static final String LoginUrl = "http://localhost:8099/changgou/login";


    @Autowired
    private AuthorService authorService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求路径
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        //判断哪些不需要令牌
        if (path.contains("/oauth/login") || !com.changgou.filter.Authorization.hasAuthorize(path)){
            return chain.filter(exchange);
        }

        //当前请求中有jti
        String jti = authorService.getJtiForCookies(request);
        if (jti == null){
            //跳转到登录页面
            response.setStatusCode(HttpStatus.SEE_OTHER);
            response.getHeaders().set("location",LoginUrl);
            return response.setComplete();
        }
        //redis中存在令牌
        String token = authorService.getTokenForRedis(jti);
        if (StringUtils.isNotEmpty(token)){
            //有令牌,通过,携带请求头
            response.setStatusCode(HttpStatus.OK);
            //携带令牌,微服务与微服务之间进行拦截,传递令牌
            request.mutate().header(Authorization,"Bearer " + token);

            return chain.filter(exchange);
        }
        //失效或者没有令牌,拒绝访问,跳转登录页面
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().set("location",LoginUrl);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
