package com.changgou.oauth.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/oauth")
public class AuthController {


    @Autowired
    private AuthService authService;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private Integer cookieMaxAge;

    //申请完毕后，才允许畅购系统使用Oauth2.0认证系统进行认证；
    //客户端id
    @Value("${auth.clientId}")
    private String clientId;

    //客户端密码
    @Value("${auth.clientSecret}")
    private String clientSecret;


    @RequestMapping("/login")  //授权
    public Result createJwt(String username , String password , HttpServletResponse response){


        if (StringUtils.isEmpty(username)){
            throw new RuntimeException("没有填写用户名");
        }

        if (StringUtils.isEmpty(password)){
            throw new RuntimeException("没有填写密码");
        }

        //调用service,返回值AuthToken


        try {
            AuthToken authToken = authService.createJwt(username,password,clientId,clientSecret);

            //将jti存到cookie中
            Cookie cookie = new Cookie("jti",authToken.getJti());
            //设置path
            cookie.setPath("/");
            //domain
            cookie.setDomain(cookieDomain);
            //maxAge
            cookie.setMaxAge(cookieMaxAge);
            //httpOnly
            cookie.setHttpOnly(false);
            //发送cookie
            response.addCookie(cookie);

            return new Result(true, StatusCode.OK,"登陆成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.OK,"登陆失败");
        }
    }

}
