package com.forteach.wechat.mini.app.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.forteach.wechat.mini.app.annotation.PassToken;
import com.forteach.wechat.mini.app.annotation.UserLoginToken;
import com.forteach.wechat.mini.app.exception.UserLoginException;
import com.forteach.wechat.mini.app.service.TokenService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import static com.forteach.wechat.mini.app.common.Dic.USER_PREFIX;

/**
 * @Description:
 * @author: liu zhenming
 * @version: V1.0
 * @date: 2018/10/30 15:27
 */
@Slf4j
@NoArgsConstructor
public class UserLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        // 从 http 请求头中取出 token
        String token = httpServletRequest.getHeader("token");
        // 如果不是映射到方法直接通过
        if(!(object instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod)object;
        Method method =handlerMethod.getMethod();
        //检查是否有 passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if (token == null) {
                    log.error("token is null");
                    throw new UserLoginException("无token，请重新登录");
                }
                // 获取 token 中的 openId
                String openId;
                try {
                    openId = JWT.decode(token).getAudience().get(0);
                } catch (JWTDecodeException j) {
                    throw new UserLoginException("401");
                }
                if (!stringRedisTemplate.hasKey(USER_PREFIX.concat(openId))) {
                    throw new UserLoginException("用户不存在，请重新登录");
                }
                // 验证 token
                try {
                    tokenService.verifier(openId).verify(token);
                } catch (JWTVerificationException e) {
                    throw new UserLoginException("401");
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}
