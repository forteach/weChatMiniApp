package com.forteach.wechat.mini.app.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.forteach.wechat.mini.app.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.forteach.wechat.mini.app.common.Dic.*;

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

    private final HashOperations<String, String, String> hashOperations;

    @Autowired
    private TokenServiceImpl(HashOperations<String, String, String> hashOperations){
        this.hashOperations = hashOperations;
    }

    @Override
    public String createToken(String openId) {
        return JWT.create()
                .withIssuedAt(new Date())
                .withAudience(openId, TOKEN_STUDENT)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_TIME * 1000))
                .sign(Algorithm.HMAC256(salt.concat(openId)));
    }

    @Override
    public JWTVerifier verifier(String openId) {
        return JWT.require(Algorithm.HMAC256(salt.concat(openId))).build();
    }

    @Override
    public String getOpenId(HttpServletRequest request) {
        String token = request.getHeader("token");
        return JWT.decode(token).getAudience().get(0);
    }

    @Override
    public String getSessionKey(String openId) {
        return hashOperations.get(USER_PREFIX.concat(openId), "sessionKey");
    }
}
