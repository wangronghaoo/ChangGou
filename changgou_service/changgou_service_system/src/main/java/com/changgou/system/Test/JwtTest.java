package com.changgou.system.Test;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


public class JwtTest {


    public static void main(String[] args) {

        /*
        JJwt由三部分组成,头部,载荷,签名
        1.用来描述该jwt基本的信息,所使用的算法,以及类型
        2.存放有效信息的地方payload
        3.签证由3个部分组成
           1.header
           2.payload  (Base64后的)
           3.secret
           这个部分需要base64加密后的header和base64加密后的payload使用.连接组成的字符
            串，然后通过header中声明的加密方式进行加盐secret组合加密，然后就构成了jwt的第
            三部分
         */
        //创建token
        /*JwtBuilder builder = Jwts.builder().setId("666")//设置唯一编号
                .setSubject("元旦")         //设置主题
                .setIssuedAt(new Date())     //设置签发日期
                .signWith(SignatureAlgorithm.HS256,"wrhhhhh");  //设置签名,使用HS256算法
            System.out.println(builder.compact());*/  //eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjYiLCJzdWIiOiLlhYPml6YiLCJpYXQiOjE1Nzc5NjE4OTd9.RzmkVUYtlJiGg-T7pxKtfiqq2tXAsK2Av-je_Zh24C0

        //解析token
        /*
        在web应用中这个操作是由服务端进行然后发给客户端，客
        户端在下次向服务端发送请求时需要携带这个token（这就好像是拿着一张门票一样），
        那服务端接到这个token 应该解析出token中的信息（例如用户id）,根据这些信息查询数
        据库返回相应的结果。
         */
//        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjYiLCJzdWIiOiLlhYPml6YiLCJpYXQiOjE1Nzc5NjE4OTd9.RzmkVUYtlJiGg-T7pxKtfiqq2tXAsK2Av-je_Zh24C0";
//        Claims claims = Jwts.parser().setSigningKey("wrhhhhh").parseClaimsJws(jwt).getBody();
//        System.out.println(claims);  //{jti=666, sub=元旦, iat=1577961897}

        //设置过期时间
        //当前时间
//        long currentTimeMillis = System.currentTimeMillis();
//        System.out.println(currentTimeMillis); //1577963527501
//        Date date = new Date(currentTimeMillis+1000000000); //Thu Jan 02 19:12:07 CST 2020
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String format = sdf.format(date);
//        System.out.println(format);   //eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1NTUiLCJzdWIiOiLorr7nva7ov4fmnJ_ml7bpl7QiLCJleHAiOjE1Nzc5NjM1MjcsImlhdCI6MTU3Nzk2MzUyN30.bB6Y8ZzNnxUuFIrMbRbFJ8T7V-xIysGxxbbDq0cBs1k
//        JwtBuilder builder = Jwts.builder().setId("777").setSubject("设置过期时间").setExpiration(date).setIssuedAt(new Date()).signWith(SignatureAlgorithm.HS256,"wangWang");
//        System.out.println(builder.compact());

        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI3NzciLCJzdWIiOiLorr7nva7ov4fmnJ_ml7bpl7QiLCJleHAiOjE1Nzg5NjM4NjQsImlhdCI6MTU3Nzk2Mzg2NH0.dt63ponUHn_AVnasTTVqdICCwAKSb4EV-Rx-UYcE4pk";
        Claims claims = Jwts.parser().setSigningKey("wangWang").parseClaimsJws(token).getBody();
        System.out.println(claims);  //{jti=777, sub=设置过期时间, exp=1578963864, iat=1577963864}


    }


}
