package com.example.HotelBooking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtUtils {

//    表示 JWT 有效期为 6个月（单位：毫秒）
    private static final long EXPIRATION_TIME_IN_MILSEC = 100L * 60L * 60L * 24L * 30L * 6L; //this will expires in 6 months
    private SecretKey key;

//    secreteJwtString 是你在 application.properties 中配置的私钥字符串
//    key 是生成签名用的密钥（使用 HMAC-SHA256）
    @Value("${secreteJwtString}")
    private String secreteJwtString;

    @PostConstruct
    private void init() {
        byte[] keyByte = secreteJwtString.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyByte, "HmacSHA256");
    }
//    @PostConstruct：Spring 在构造对象后会自动调用这个方法，初始化加密用的密钥

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_IN_MILSEC))
                .signWith(key)
                .compact();
    }
//    输入：用户的 email
//
//    输出：生成一个有效期 6 个月的 JWT
//
//.signWith(key)：用密钥签名，防止被伪造

    public String getUsernameFromToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }
//    提取出 sub（subject）字段，即邮箱
    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
    }
//    用泛型函数从 Token 中提取各种字段（subject, expiration 等）


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
//    token 中的用户名是否与当前用户一致

//    token 是否未过期

    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
//    判断是否已经过期（过期时间在当前时间之前）


}
