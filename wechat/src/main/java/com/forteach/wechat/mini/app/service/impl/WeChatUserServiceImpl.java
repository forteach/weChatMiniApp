package com.forteach.wechat.mini.app.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.forteach.wechat.mini.app.domain.WeChatUserInfo;
import com.forteach.wechat.mini.app.repository.WeChatUserInfoRepository;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.util.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.forteach.wechat.mini.app.common.Dic.TWO_HOURS;
import static com.forteach.wechat.mini.app.common.Dic.WX_USER_PREFIX;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-8 15:05
 * @Version: 1.0
 * @Description:
 */
@Slf4j
@Service
public class WeChatUserServiceImpl implements WeChatUserService {

    @Resource
    private WeChatUserInfoRepository weChatUserInfoRepository;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void saveUser(WxMaUserInfo wxMaUserInfo){
        // 需要更新用户数据信息
        WeChatUserInfo weChatUserInfo = new WeChatUserInfo();
        UpdateUtil.copyNullProperties(wxMaUserInfo, weChatUserInfo);
        weChatUserInfoRepository.save(weChatUserInfo);
        redisTemplate.opsForValue().set(WX_USER_PREFIX + wxMaUserInfo.getOpenId(), weChatUserInfo.toString(), TWO_HOURS);
    }
}
