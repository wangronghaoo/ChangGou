package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {


    @Autowired
    private RestTemplate restTemplate;
    /**
     * 生成令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */

    @Autowired
    private RedisTemplate redisTemplate;

    //redis过期时间
    @Value("${auth.ttl}")
    private Integer ttl;

    @Override
    public AuthToken createJwt(String username, String password, String clientId, String clientSecret) {

        //使用restTemplate发送请求地址,根据服务名称进行发送(可能搭载集群)
        String url = "http://user-auth/oauth/token";


        //请求体
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        //密码申请
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        //请求头
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();

        //客户端id : 客户端密码
        //Authorization :   Basic d2FuZ3JvbmdoYW86Y2hhbmdnb3U=
        String clientValue = this.baseToString(clientId, clientSecret);
        headers.add("authorization",clientValue);

        //封装请求体
        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(body,headers);
        ResponseEntity<Map> entity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        //获取相关令牌信息
        Map jwtBody = entity.getBody();

        if (jwtBody == null || jwtBody.get("access_token") == null || jwtBody.get("refresh_token") == null || jwtBody.get("jti") == null){
            throw new RuntimeException("申请令牌失败");
        }

        String access_token = (String) jwtBody.get("access_token");
        String refresh_token = (String) jwtBody.get("refresh_token");
        String jti = (String) jwtBody.get("jti");

        //将jti作为键,jwt作为值,存入redis
        redisTemplate.boundValueOps(jwtBody.get("jti")).set(jwtBody.get("access_token"),ttl, TimeUnit.SECONDS);
        return new AuthToken(access_token,refresh_token,jti);
    }



    /**
     * 客户端id,客户端密码
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @return String
     */
    private String baseToString(String clientId, String clientSecret) {
        byte[] valueBytes = (clientId + ":" + clientSecret).getBytes();
        String clientBytes = Base64Utils.encodeToString(valueBytes);
        return "Basic " + clientBytes;

    }
}
