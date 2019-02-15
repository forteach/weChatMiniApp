package com.forteach.wechat.mini.app.service;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.web.req.BindingUserInfoReq;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 12:05
 * @Version: 1.0
 * @Description:
 */
public interface WeChatUserService {
    void saveUser(WxMaUserInfo wxMaUserInfo) throws Exception;

    /**
     * 绑定微信登录学号和 openId, 进行身份校验，通过取redis 数据库比较
     * @param bindingUserInfoReq
     * @return WebResult
     */
    WebResult bindingUserInfo(BindingUserInfoReq bindingUserInfoReq);
}
