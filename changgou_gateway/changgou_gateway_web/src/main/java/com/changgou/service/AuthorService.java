package com.changgou.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class AuthorService {


    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 获取jti
     * @param request
     * @return
     */
    public String getJtiForCookies(ServerHttpRequest request){
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie jti = cookies.getFirst("jti");
        if (jti != null){
            return jti.getValue();
        }
        return null;
    }


    /**
     * 查看令牌是否过期
     * @param jti
     * @return
     */
    public String getTokenForRedis(String jti){
        String jwt = (String) redisTemplate.boundValueOps(jti).get();
        return jwt;
    }
}
