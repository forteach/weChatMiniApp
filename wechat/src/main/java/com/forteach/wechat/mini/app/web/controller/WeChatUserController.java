package com.forteach.wechat.mini.app.web.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.StrUtil;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.config.WeChatMiniAppConfig;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

    @Autowired
    public WeChatUserController(WeChatUserService weChatUserService) {
        this.weChatUserService = weChatUserService;
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
            return WebResult.okResult(JsonUtils.toJson(session));
        } catch (WxErrorException e) {
            this.logger.error(e.getMessage(), e);
            return WebResult.failException(e.toString());
        }
    }

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
}
