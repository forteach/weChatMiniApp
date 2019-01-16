package com.forteach.wechat.mini.app.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.forteach.wechat.mini.app.domain.WeChatUserInfo;
import com.forteach.wechat.mini.app.repository.WeChatUserInfoRepository;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.util.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 保存用户通过微信登录进来的信息到mysql 和 redis(设置２个小时有效期)
     * @param wxMaUserInfo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(WxMaUserInfo wxMaUserInfo){
        // 需要更新用户数据信息
        Optional<WeChatUserInfo> optionalWeChatUserInfo = weChatUserInfoRepository.findByOpenId(wxMaUserInfo.getOpenId()).findFirst();
        WeChatUserInfo weChatUserInfo = optionalWeChatUserInfo.orElseGet(WeChatUserInfo::new);
        BeanUtils.copyProperties(wxMaUserInfo, weChatUserInfo);
        weChatUserInfoRepository.save(weChatUserInfo);
        //保存redis 设置有效期2个小时
        Map<String, Object> map = MapUtil.objectToMap(weChatUserInfo);
        String key = WX_USER_PREFIX + wxMaUserInfo.getOpenId();
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, 2, TimeUnit.HOURS);
    }
}
