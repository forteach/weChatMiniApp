package com.forteach.wechat.mini.app.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.util.StrUtil;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.config.WeChatMiniAppConfig;
import com.forteach.wechat.mini.app.domain.StudentEntitys;
import com.forteach.wechat.mini.app.domain.WeChatUserInfo;
import com.forteach.wechat.mini.app.dto.IWeChatUserInfo;
import com.forteach.wechat.mini.app.repository.StudentRepository;
import com.forteach.wechat.mini.app.repository.WeChatUserInfoRepository;
import com.forteach.wechat.mini.app.service.TokenService;
import com.forteach.wechat.mini.app.service.WeChatUserService;
import com.forteach.wechat.mini.app.util.MapUtil;
import com.forteach.wechat.mini.app.util.UpdateUtil;
import com.forteach.wechat.mini.app.web.req.BindingUserInfoReq;
import com.forteach.wechat.mini.app.web.req.WeChatUserInfoReq;
import com.forteach.wechat.mini.app.web.resp.LoginResp;
import com.forteach.wechat.mini.app.web.vo.WxDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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

    private final TokenService tokenService;


    @Autowired
    public WeChatUserServiceImpl(StudentRepository studentRepository,
                                 WeChatUserInfoRepository weChatUserInfoRepository,
                                 StringRedisTemplate stringRedisTemplate,
                                 TokenService tokenService) {
        this.studentRepository = studentRepository;
        this.weChatUserInfoRepository = weChatUserInfoRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebResult bindingUserInfo(BindingUserInfoReq bindingUserInfoReq, HttpServletRequest request) {
        Optional<StudentEntitys> studentEntitys = studentRepository.findByIsValidatedEqualsAndIdCardNo(TAKE_EFFECT_OPEN, bindingUserInfoReq.getIdCardNo())
                .stream()
                .filter(Objects::nonNull)
                .findFirst();
        if (studentEntitys.isPresent()) {
            Optional<WeChatUserInfo> weChatUserInfoOptional = weChatUserInfoRepository.findByOpenId(tokenService.getOpenId(request))
                    .stream()
                    .filter(Objects::nonNull)
                    .findFirst();
            if (weChatUserInfoOptional.isPresent() && WX_INFO_BINDIND_0.equals(weChatUserInfoOptional.get().getBinding())) {
                return WebResult.failException("该微信账号已经认证");
            }
            WeChatUserInfo weChatUserInfo = weChatUserInfoOptional.orElseGet(WeChatUserInfo::new);
            if (checkStudent(bindingUserInfoReq, studentEntitys.get())) {
                final WxMaService wxService = WeChatMiniAppConfig.getMaService();
                String openId = tokenService.getOpenId(request);
                String sessionKey = tokenService.getSessionKey(openId);
                String key = USER_PREFIX.concat(openId);
                // 用户信息校验
                WxMaUserInfo wxMaUserInfo = null;
                if (checkWxInfo(sessionKey, wxService, bindingUserInfoReq)) {
                    // 解密用户信息
                    wxMaUserInfo = wxService.getUserService().getUserInfo(sessionKey, bindingUserInfoReq.getEncryptedData(), bindingUserInfoReq.getIv());
                }
                // 需要更新用户数据信息
                if (wxMaUserInfo != null) {
                    BeanUtils.copyProperties(wxMaUserInfo, weChatUserInfo);
                }
                weChatUserInfo.setBinding(WX_INFO_BINDIND_0);
                weChatUserInfo.setStudentId(studentEntitys.get().getId());
                weChatUserInfo.setClassId(studentEntitys.get().getClassId());
                weChatUserInfo.setOpenId(openId);
                weChatUserInfoRepository.save(weChatUserInfo);
                //保存redis 设置有效期7天
                Map<String, Object> map = MapUtil.objectToMap(weChatUserInfo);
                //设置token类型为学生微信登录
                map.put("type", TOKEN_STUDENT);
                stringRedisTemplate.opsForHash().putAll(key, map);
                stringRedisTemplate.expire(key, TOKEN_VALIDITY_TIME, TimeUnit.SECONDS);
                return WebResult.okResult("绑定成功");
            }
        }
        return WebResult.failException("身份信息不符, 请联系管理员");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebResult bindingToken(WxMaJscode2SessionResult session, String portrait) {
        String openId = session.getOpenid();
        String token = tokenService.createToken(openId);
        String binding = WX_INFO_BINDIND_1;

        Optional<WeChatUserInfo> weChatUserInfoOptional = weChatUserInfoRepository.findByOpenId(openId).stream().findFirst();
        if (weChatUserInfoOptional.isPresent()) {
            binding = weChatUserInfoOptional.get().getBinding();
        }

        Map<String, Object> map = MapUtil.objectToMap(weChatUserInfoOptional.orElse(new WeChatUserInfo()));
        map.put("openId", openId);
        map.put("sessionKey", openId);
        map.put("token", token);
        map.put("binding", binding);
        String key = USER_PREFIX.concat(openId);
        stringRedisTemplate.opsForHash().putAll(key, map);
        //设置有效期7天
        stringRedisTemplate.expire(key, TOKEN_VALIDITY_TIME, TimeUnit.SECONDS);

        weChatUserInfoOptional.ifPresent(weChatUserInfo -> {
            studentRepository.findById(weChatUserInfo.getStudentId()).ifPresent(studentEntitys -> {
                if (StrUtil.isNotBlank(portrait)) {
                    weChatUserInfo.setAvatarUrl(portrait);
                    weChatUserInfoRepository.save(weChatUserInfo);
                }
                studentEntitys.setPortrait(portrait);
                studentRepository.save(studentEntitys);
                String studentKey = STUDENT_ADO.concat(weChatUserInfo.getStudentId());
                stringRedisTemplate.opsForHash().put(studentKey, "portrait", portrait);
            });
        });

        IWeChatUserInfo iWeChatUserInfo = weChatUserInfoRepository.findByIsValidatedEqualsAndOpenId(openId);
        LoginResp loginResp = new LoginResp();
        if (iWeChatUserInfo != null) {
            loginResp.setClassId(iWeChatUserInfo.getClassId());
            loginResp.setClassName(iWeChatUserInfo.getClassName());
            loginResp.setPortrait(iWeChatUserInfo.getPortrait());
            loginResp.setStudentId(iWeChatUserInfo.getStudentId());
            loginResp.setStudentName(iWeChatUserInfo.getStudentName());
        }
        loginResp.setBinding(binding);
        loginResp.setToken(token);
        return WebResult.okResult(loginResp);
    }

    @Override
    public WebResult getBindingPhone(WxDataVo wxDataVo, HttpServletRequest request) {
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();
        String openId = tokenService.getOpenId(request);
        String sessionKey = tokenService.getSessionKey(openId);
        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, wxDataVo.getRawData(), wxDataVo.getSignature())) {
            return WebResult.failException("user check failed");
        }
        WxMaPhoneNumberInfo phoneNoInfo = wxService.getUserService().getPhoneNoInfo(sessionKey, wxDataVo.getEncryptedData(), wxDataVo.getIv());
        return WebResult.okResult(phoneNoInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebResult restart(String string) {
        List<WeChatUserInfo> list = weChatUserInfoRepository.findByStudentId(string);
        if (list.size() > 0) {
            list.stream().filter(Objects::nonNull)
                    .forEach(weChatUserInfo -> {
                        weChatUserInfoRepository.delete(weChatUserInfo);
                    });
            return WebResult.okResult();
        } else {
            return WebResult.okResult("不存要删除的用户");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WebResult saveWeChatUserInfo(WeChatUserInfoReq weChatUserInfoReq, HttpServletRequest request) {
        String openId = tokenService.getOpenId(request);
        Optional<WeChatUserInfo> optionalWeChatUserInfo = weChatUserInfoRepository.findByOpenId(openId).stream().filter(Objects::nonNull).findFirst();
        if (optionalWeChatUserInfo.isPresent()) {
            WeChatUserInfo weChatUserInfo = optionalWeChatUserInfo.get();
            UpdateUtil.copyNullProperties(weChatUserInfoReq, weChatUserInfo);
            Optional<StudentEntitys> studentEntitysOptional = studentRepository.findById(weChatUserInfo.getStudentId());
            if (studentEntitysOptional.isPresent()) {
                StudentEntitys studentEntitys = studentEntitysOptional.get();
                studentEntitys.setPortrait(weChatUserInfoReq.getAvatarUrl());
                studentRepository.save(studentEntitys);
            }
            weChatUserInfoRepository.save(weChatUserInfo);
            return WebResult.okResult();
        } else {
            return WebResult.failException("用户不存在");
        }
    }

    /**
     * 校验身份证和姓名在数据库中是否存在
     *
     * @param bindingUserInfoReq
     * @param studentEntitys
     * @return
     */
    private boolean checkStudent(BindingUserInfoReq bindingUserInfoReq, StudentEntitys studentEntitys) {
        return studentEntitys.getUserName().equals(bindingUserInfoReq.getUserName())
                && studentEntitys.getIdCardNo().equals(bindingUserInfoReq.getIdCardNo());
    }

    /**
     * 校验是否是微信发送的数据
     *
     * @param sessionKey
     * @param wxService
     * @param bindingUserInfoReq
     * @return
     */
    private boolean checkWxInfo(String sessionKey, WxMaService wxService, BindingUserInfoReq bindingUserInfoReq) {
        if (StrUtil.isNotBlank(bindingUserInfoReq.getEncryptedData())
                && StrUtil.isNotBlank(bindingUserInfoReq.getSignature())
                && StrUtil.isNotBlank(bindingUserInfoReq.getIv())
                && StrUtil.isNotBlank(bindingUserInfoReq.getRawData())) {
            return wxService.getUserService().checkUserInfo(sessionKey,
                    bindingUserInfoReq.getRawData(), bindingUserInfoReq.getSignature());
        }
        return false;
    }
}
