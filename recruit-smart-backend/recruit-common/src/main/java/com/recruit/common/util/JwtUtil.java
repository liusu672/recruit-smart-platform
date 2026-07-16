package com.recruit.common.util;

import com.recruit.common.enums.ErrorCode;
import com.recruit.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expire-time}")
    private Long expire;
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(Long userId,String username,String roleCode){
        Date now=new Date();
        Date expireTime=new Date(now.getTime()+expire);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId",userId)
                .claim("username",username)
                .claim("roleCode",roleCode)
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(getSecretKey())
                .compact();
    }
    public Claims parseToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch(JwtException | IllegalArgumentException e){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"登录状态已失效");
        }
    }
}
