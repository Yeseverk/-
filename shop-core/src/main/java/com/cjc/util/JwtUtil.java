package com.cjc.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private static String key = "cjc-shop-secret-key-for-jwt-token-generation-2026";
    private static long ttl = 30*24*60*60*1000;

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成jwt
     *
     * @param id
     * @param subject
     * @param roles
     * @return
     */
    public static String createJwt(String id, String subject, String roles) {
        long millis = System.currentTimeMillis();
        Date date = new Date(millis);

        var jwtBuilder = Jwts.builder()
                .id(id)
                .subject(subject)
                .issuedAt(date)
                .claim("roles", roles)
                .signWith(getSecretKey());

        if (ttl > 0) {
            jwtBuilder.expiration(new Date(millis + ttl));
        }
        return jwtBuilder.compact();
    }

    /**
     * 解密jwt
     * @param jwtStr
     * @return
     */
    public static Claims parseJwt(String jwtStr) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(jwtStr)
                .getPayload();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}