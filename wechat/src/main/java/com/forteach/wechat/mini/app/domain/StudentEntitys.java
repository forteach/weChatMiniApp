package com.forteach.wechat.mini.app.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-7 09:39
 * @Version: 1.0
 * @Description: 从学校查询其它系统导入的学生数据表
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student_info",indexes = {@Index(columnList = "id"), @Index(columnList = "id_card_no")})
@org.hibernate.annotations.Table(appliesTo = "student_info", comment = "从学校数据库查询的学生信息")
public class StudentEntitys extends Entitys {
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(32) COMMENT '学号id'")
    private String id;

    @Column(name = "user_name", columnDefinition = "VARCHAR(32) COMMENT '用户名'")
    private String userName;

    @Column(name = "id_card_no", columnDefinition = "VARCHAR(32) COMMENT '身份证号码'")
    private String idCardNo;

}
