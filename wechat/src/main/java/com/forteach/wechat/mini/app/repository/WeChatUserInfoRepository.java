package com.forteach.wechat.mini.app.repository;

import com.forteach.wechat.mini.app.domain.WeChatUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.stream.Stream;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 17:10
 * @Version: 1.0
 * @Description:
 */
public interface WeChatUserInfoRepository extends JpaRepository<WeChatUserInfo, String> {
    Stream<WeChatUserInfo> findByOpenId(String openId);
}
