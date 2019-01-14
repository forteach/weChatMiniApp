package com.forteach.wechat.mini.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 16:44
 * @Version: 1.0
 * @Description:
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wx_userinfo", indexes = {
        @Index(columnList = "id"),
        @Index(columnList = "open_id")
})
@EqualsAndHashCode(callSuper = true)
@org.hibernate.annotations.Table(appliesTo = "wx_userinfo", comment = "微信用户信息")
public class WeChatUserInfo extends Entitys {

    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    @Column(name = "id", columnDefinition = "VARCHAR(32) comment 'id'", unique = true)
    private String id;

    @Column(name = "open_id", columnDefinition = "VARCHAR(32) COMMENT '微信openId'", unique = true)
    private String openId;

    @Column(name = "nick_name", columnDefinition = "VARCHAR(32) COMMENT '用户微信昵称'")
    private String nickName;

    @Column(name = "gender", columnDefinition = "CHAR(1) COMMENT '用户性别 0 未知 1男性　2 女性'")
    private String gender;

    @Column(name = "language", columnDefinition = "VARCHAR(32) COMMENT '显示所用的语言 en 英文 zh_CN 简体中文 zh_TW 繁体中文'")
    private String language;

    @Column(name = "city", columnDefinition = "VARCHAR(60) COMMENT '用户所在城市'")
    private String city;

    @Column(name = "province", columnDefinition = "VARCHAR(60) COMMENT '用户所在的省份'")
    private String province;

    @Column(name = "country", columnDefinition = "VARCHAR(32) COMMENT '用户所在的国家'")
    private String country;

    @Column(name = "avatar_url", columnDefinition = "VARCHAR(256) COMMENT '用户的头像URL'")
    private String avatarUrl;
}
