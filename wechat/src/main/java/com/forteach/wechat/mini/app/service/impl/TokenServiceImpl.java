package com.forteach.wechat.mini.app.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.forteach.wechat.mini.app.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 2019/2/17 17:27
 * @Version: 1.0
 * @Description:
 */
@Service(value = "TokenService")
public class TokenServiceImpl implements TokenService {

    @Value("${token.salt}")
    private String salt;

    @Override
    public String createToken(String openId) {
        return JWT.create().withAudience(openId)
                .sign(Algorithm.HMAC256(salt.concat(openId)));
    }

    @Override
    public JWTVerifier verifier(String openId) {
        return JWT.require(Algorithm.HMAC256(salt.concat(openId))).build();
    }
}
