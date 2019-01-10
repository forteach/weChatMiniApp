package com.forteach.wechat.mini.app.service;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 12:05
 * @Version: 1.0
 * @Description:
 */
public interface WeChatUserService {
    void saveUser(WxMaUserInfo wxMaUserInfo);
}
