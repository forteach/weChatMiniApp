package com.forteach.wechat.mini.app.web.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.alibaba.fastjson.JSONObject;
import com.forteach.wechat.mini.app.annotation.PassToken;
import com.forteach.wechat.mini.app.annotation.UserLoginToken;
import com.forteach.wechat.mini.app.common.DefineCode;
import com.forteach.wechat.mini.app.common.MyAssert;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.config.WeChatMiniAppConfig;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.web.req.BindingUserInfoReq;
import com.forteach.wechat.mini.app.web.vo.WxDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;


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
    @PassToken
    public WebResult login(String code){
        MyAssert.blank(code, DefineCode.ERR0010, "code is null");
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();
        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
            this.logger.info(session.getSessionKey());
            this.logger.info(session.getOpenid());
            //TODO 可以增加自己的逻辑，关联业务相关数据
            return weChatUserService.bindingToken(session);
        } catch (WxErrorException e) {
            this.logger.error(e.getMessage(), e);
            return WebResult.failException(e.getMessage());
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "signature", value = "sha1( rawData + session_key )", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "rawData", value = "rawData", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "encryptedData", value = "加密数据", dataType = "string", required = true, paramType = "query"),
            @ApiImplicitParam(name = "iv", value = "数据接口返回", dataType = "string", required = true, paramType = "query"),
    })
    @UserLoginToken
    @PostMapping("/phone")
    @ApiOperation(value = "获取用户绑定手机号信息", notes = "获取用户绑定手机号信息")
    public WebResult getBingPhone(@RequestBody WxDataVo wxDataVo, HttpServletRequest request){
        // 绑定手机
        return weChatUserService.getBindingPhone(wxDataVo, request);
    }

    @UserLoginToken
    @ApiOperation(value = "绑定微信用户登录信息")
    @PostMapping("/binding")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "身份证姓名", required = true, paramType = "from"),
            @ApiImplicitParam(name = "idCardNo", value = "身份证号码", required = true, paramType = "from"),
            @ApiImplicitParam(name = "signature", value = "sha1( rawData + session_key )", dataType = "string", paramType = "from"),
            @ApiImplicitParam(name = "rawData", value = "rawData", dataType = "string", paramType = "from"),
            @ApiImplicitParam(name = "encryptedData", value = "加密数据", dataType = "string", paramType = "from"),
            @ApiImplicitParam(name = "iv", value = "数据接口返回", dataType = "string", paramType = "from"),
    })
    public WebResult binding(@RequestBody BindingUserInfoReq bindingUserInfoReq, HttpServletRequest request){
        MyAssert.blank(bindingUserInfoReq.getIdCardNo(), DefineCode.ERR0010, "身份证号码不为空");
        MyAssert.blank(bindingUserInfoReq.getUserName(), DefineCode.ERR0010, "用户名不为空");
        return weChatUserService.bindingUserInfo(bindingUserInfoReq, request);
    }

    @DeleteMapping("/restart")
    public WebResult restart(@RequestBody String studentId){
        String string = JSONObject.parseObject(studentId).getString("studentId");
        return weChatUserService.restart(string);
    }
}
