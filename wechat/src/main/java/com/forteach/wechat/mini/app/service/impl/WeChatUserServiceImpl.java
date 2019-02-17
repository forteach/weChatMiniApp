package com.forteach.wechat.mini.app.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.domain.StudentEntitys;
import com.forteach.wechat.mini.app.domain.WeChatUserInfo;
import com.forteach.wechat.mini.app.repository.StudentRepository;
import com.forteach.wechat.mini.app.repository.WeChatUserInfoRepository;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.util.MapUtil;
import com.forteach.wechat.mini.app.web.req.BindingUserInfoReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.forteach.wechat.mini.app.common.Dic.*;

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

    private final StudentRepository studentRepository;

    private final WeChatUserInfoRepository weChatUserInfoRepository;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public WeChatUserServiceImpl(StudentRepository studentRepository, WeChatUserInfoRepository weChatUserInfoRepository, StringRedisTemplate stringRedisTemplate, RedisTemplate<String, String> redisTemplate) {
        this.studentRepository = studentRepository;
        this.weChatUserInfoRepository = weChatUserInfoRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 保存用户通过微信登录进来的信息到mysql 和 redis(设置７天有效期)
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
        //保存redis 设置有效期7天
        Map<String, Object> map = MapUtil.objectToMap(weChatUserInfo);
        String key = WX_USER_PREFIX + wxMaUserInfo.getOpenId();
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebResult bindingUserInfo(BindingUserInfoReq bindingUserInfoReq) {
        Optional<StudentEntitys> studentEntitys = studentRepository.findByIsValidatedEqualsAndIdCardNo(TAKE_EFFECT_OPEN, bindingUserInfoReq.getIdCardNo())
                .stream()
                .filter(Objects::nonNull)
                .findFirst();
        if (studentEntitys.isPresent()) {
            WeChatUserInfo weChatUserInfo = weChatUserInfoRepository.findByOpenId(bindingUserInfoReq.getOpenId()).filter(Objects::nonNull).findFirst().get();
            if (WX_INFO_BINDIND_0.equals(weChatUserInfo.getBinding())){
                return WebResult.failException("该微信账号已经认证");
            }
            if (checkStudent(bindingUserInfoReq, studentEntitys.get())) {
                this.updateWeChatBindingInfo(weChatUserInfo, studentEntitys.get());
                return WebResult.okResult("绑定成功");
            }
        }
        return WebResult.failException("身份信息不符, 请联系管理员");
    }

    /**
     * 保存用户微信绑定的信息
     * @param weChatUserInfo
     * @param studentEntitys
     */
    private void updateWeChatBindingInfo(WeChatUserInfo weChatUserInfo, StudentEntitys studentEntitys) {
        weChatUserInfo.setStudentId(studentEntitys.getId());
        weChatUserInfo.setBinding(WX_INFO_BINDIND_0);
        weChatUserInfoRepository.save(weChatUserInfo);
    }

    /**
     * 校验身份证和姓名在数据库中是否存在
     * @param bindingUserInfoReq
     * @param studentEntitys
     * @return
     */
    private boolean checkStudent(BindingUserInfoReq bindingUserInfoReq, StudentEntitys studentEntitys){
        return studentEntitys.getUserName().equals(bindingUserInfoReq.getUserName())
                && studentEntitys.getIdCardNo().equals(bindingUserInfoReq.getIdCardNo());
    }
}
