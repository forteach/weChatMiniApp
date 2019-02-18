package com.forteach.wechat.mini.app.repository;

import com.forteach.wechat.mini.app.domain.WeChatUserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Stream;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 17:10
 * @Version: 1.0
 * @Description:
 */
public interface WeChatUserInfoRepository extends JpaRepository<WeChatUserInfo, String> {
    /**
     * 根据微信账号查询绑定学生信息
     * @param openId
     * @return
     */
    @Transactional(readOnly = true)
    Stream<WeChatUserInfo> findByOpenId(String openId);
}
