package com.forteach.wechat.mini.app.service;

import com.auth0.jwt.JWTVerifier;
import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 2019/2/17 17:24
 * @Version: 1.0
 * @Description:
 */
public interface TokenService {
    /**
     * 用微信openId生成一个一天有效期的token
     * @param openId
     * @return
     */
    String createToken(String openId);

    /**
     * 获取JWT验证
     * @param openId
     * @return
     */
    JWTVerifier verifier(String openId);

    /**
     * 获取微信openId
     * @param request
     * @return
     */
    String getOpenId(HttpServletRequest request);

    /**
     * 获取用户的 session-key
     * @param openId
     * @return
     */
    String getSessionKey(String openId);
}
