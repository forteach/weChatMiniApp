package com.forteach.wechat.mini.app.web.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.StrUtil;
import com.forteach.wechat.mini.app.annotation.UserLoginToken;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.config.WeChatMiniAppConfig;
import com.forteach.wechat.mini.app.service.TokenService;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.util.JsonUtils;
import com.forteach.wechat.mini.app.web.req.BindingUserInfoReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.forteach.wechat.mini.app.common.Dic.WX_USER_PREFIX;


/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 11:19
 * @Version: 1.0
 * @Description:
 */
@Api(value = "微信用户操作信息", description = "用户操作相关接口", tags = {"微信用户操作"})
@RestController
@RequestMapping("/user")
public class WeChatUserController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WeChatUserService weChatUserService;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenService tokenService;

    @Autowired
    public WeChatUserController(WeChatUserService weChatUserService, StringRedisTemplate stringRedisTemplate, TokenService tokenService) {
        this.weChatUserService = weChatUserService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenService = tokenService;
    }

    @ApiOperation(value = "微信小程序登录接口", notes = "微信小程序登录接口")
    @GetMapping("/login")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "微信登录凭证(code)", dataType = "string", required = true, paramType = "query")
    })
    public WebResult login(String code){
        if (StrUtil.isBlank(code)){
            return WebResult.failException("code NotBlank");
        }
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();

        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
            this.logger.info(session.getSessionKey());
            this.logger.info(session.getOpenid());
            //TODO 可以增加自己的逻辑，关联业务相关数据
            Map<String, String> map = new HashMap<>(2);
            map.put("openId", session.getOpenid());
            map.put("sessionKey", session.getSessionKey());
            //生成tocken 保存 redis
            map.put("token", tokenService.createToken(session.getOpenid()));
            String key = WX_USER_PREFIX.concat(session.getOpenid());
            stringRedisTemplate.opsForHash().putAll(key, map);
            //设置有效期一天
            stringRedisTemplate.expire(key, 1L, TimeUnit.DAYS);
            return WebResult.okResult(JsonUtils.toJson(map));
        } catch (WxErrorException e) {
            this.logger.error(e.getMessage(), e);
            return WebResult.failException(e.toString());
        }
    }

    @UserLoginToken
    @GetMapping("/info")
    @ApiOperation(value = "获取用户信息", notes = "获取用户下信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sessionKey", value = "用户的 session-key", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "signature", value = "sha1( rawData + session_key )", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "rawData", value = "rawData", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "encryptedData", value = "加密数据", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "iv", value = "数据接口返回", dataType = "string", required = true, paramType = "query"),
    })
    public WebResult getWeChatInfo(String sessionKey, String signature, String rawData, String encryptedData, String iv) throws Exception {
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();
        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            return WebResult.failException("user check failed");
        }
        // 解密用户信息
        WxMaUserInfo userInfo = wxService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
        //保存查询到的微信用户信息
        weChatUserService.saveUser(userInfo);
        return WebResult.okResult(JsonUtils.toJson(userInfo));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "sessionKey", value = "用户的 session-key", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "signature", value = "sha1( rawData + session_key )", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "rawData", value = "rawData", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "encryptedData", value = "加密数据", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "iv", value = "数据接口返回", dataType = "string", required = true, paramType = "query"),
    })
    @UserLoginToken
    @GetMapping("/phone")
    @ApiOperation(value = "获取用户绑定手机号信息", notes = "获取用户绑定手机号信息")
    public WebResult getBingPhone(String sessionKey, String signature,
                                  String rawData, String encryptedData, String iv){
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();
        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            return WebResult.failException("user check failed");
        }
        // 解密
        WxMaPhoneNumberInfo phoneNoInfo = wxService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
        return WebResult.okResult(JsonUtils.toJson(phoneNoInfo));
    }

    @UserLoginToken
    @ApiOperation(value = "绑定微信用户登录信息")
    @PostMapping("/binding")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "openId", value = "微信用户openId", required = true, paramType = "from"),
            @ApiImplicitParam(name = "userName", value = "身份证姓名", required = true, paramType = "from"),
            @ApiImplicitParam(name = "idCardNo", value = "身份证号码", required = true, paramType = "from")
    })
    public WebResult binding(@Valid @RequestBody BindingUserInfoReq bindingUserInfoReq){
        return weChatUserService.bindingUserInfo(bindingUserInfoReq);
    }
}
