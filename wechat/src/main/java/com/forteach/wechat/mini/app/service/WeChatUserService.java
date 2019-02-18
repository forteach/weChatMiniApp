package com.forteach.wechat.mini.app.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.web.req.BindingUserInfoReq;
import com.forteach.wechat.mini.app.web.vo.WxDataVo;
import javax.servlet.http.HttpServletRequest;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 12:05
 * @Version: 1.0
 * @Description:
 */
public interface WeChatUserService {
    /**
     * 绑定微信登录学号和 openId, 进行身份校验，通过取redis 数据库比较
     * @param bindingUserInfoReq
     * @return WebResult
     */
    WebResult bindingUserInfo(BindingUserInfoReq bindingUserInfoReq, HttpServletRequest request);

    /**
     * 生成token并绑定用户上
     * @param session
     * @return
     */
    WebResult bindingToken(WxMaJscode2SessionResult session);

    /**
     * 获取绑定手机号码信息
     */
    WebResult getBindingPhone(WxDataVo wxDataVo, HttpServletRequest request);
}
