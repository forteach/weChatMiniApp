package com.forteach.wechat.mini.app.web.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import com.forteach.wechat.mini.app.config.WeChatMiniAppConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 15:51
 * @Version: 1.0
 * @Description: 接搜微信服务器返回的数据
 */
@RestController
@Api("微信验证")
@RequestMapping("/portal/{appid}")
public class WechatPortalController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiOperation("验证微信发送的消息是否合法")
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@PathVariable("appid") String appid,
                          @RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        this.logger.info("\n接收到来自微信服务器的认证消息：signature = [{}], timestamp = [{}], nonce = [{}], echostr = [{}]",
                signature, timestamp, nonce, echostr);

        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();

        if (wxService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "非法请求";
    }

    @ApiOperation("验证微信加密信息判断类型")
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@PathVariable("appid") String appid,
                       @RequestBody String requestBody,
                       @RequestParam("msg_signature") String msgSignature,
                       @RequestParam("encrypt_type") String encryptType,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce) {
        this.logger.info("\n接收微信请求：[msg_signature=[{}], encrypt_type=[{}], signature=[{}]," +
                        " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                msgSignature, encryptType, signature, timestamp, nonce, requestBody);

        final WxMaService wxService = WeChatMiniAppConfig.getMaService();

        final boolean isJson = Objects.equals(wxService.getWxMaConfig().getMsgDataFormat(),
                WxMaConstants.MsgDataFormat.JSON);
        if (StringUtils.isBlank(encryptType)) {
            // 明文传输的消息
            WxMaMessage inMessage;
            if (isJson) {
                inMessage = WxMaMessage.fromJson(requestBody);
            } else {//xml
                inMessage = WxMaMessage.fromXml(requestBody);
            }
            this.route(inMessage,appid);
            return "success";
        }
        if ("aes".equals(encryptType)) {
            // 是aes加密的消息
            WxMaMessage inMessage;
            if (isJson) {
                inMessage = WxMaMessage.fromEncryptedJson(requestBody, wxService.getWxMaConfig());
            } else {//xml
                inMessage = WxMaMessage.fromEncryptedXml(requestBody, wxService.getWxMaConfig(),
                        timestamp, nonce, msgSignature);
            }
            this.route(inMessage, appid);
            return "success";
        }
        throw new RuntimeException("不可识别的加密类型：" + encryptType);
    }

    private void route(WxMaMessage message, String appid) {
        try {
            WeChatMiniAppConfig.getRouters().get(appid).route(message);
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}
