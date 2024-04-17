package edu.hubu.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String KEY;
    @Value("${spring.security.jwt.expire}")
    int EXPIRE;
    public DecodedJWT resolveJwt(String headerToken){
        String token = this.convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(KEY);
        JWTVerifier jwtVerify = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerify.verify(token);
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ?null:verify;
        }catch (JWTVerificationException e){
            return null;
        }
    }
    public String creatJwt(UserDetails userDetails,int id,String username){
        Algorithm algorithm = Algorithm.HMAC256(KEY);
        return JWT.create()
                .withClaim("id",id)
                .withClaim("username",username)
                .withClaim("authorities",userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireTime())
                .withIssuedAt(new Date())
                .sign(algorithm);

    }
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR,EXPIRE*24);
        return calendar.getTime();
    }
    private String convertToken(String headerToken){
        if(headerToken == null || !headerToken.startsWith("Bearer ")) return null;
        return headerToken.substring(7);

    }
    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claimMap = jwt.getClaims();
        return User.withUsername(claimMap.get("username").asString())
                .password("******")
//                .authorities(claimMap.get("authorities").asString())
                .build();
    }
    public Integer toInt(DecodedJWT jwt){
        Map<String, Claim> claimMap = jwt.getClaims();
        return claimMap.get("id").asInt();
    }
}
