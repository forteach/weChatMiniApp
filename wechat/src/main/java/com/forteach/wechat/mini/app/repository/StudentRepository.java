package com.forteach.wechat.mini.app.repository;

import com.forteach.wechat.mini.app.domain.StudentEntitys;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author: zhangyy
 * @email: zhang10092009@hotmail.com
 * @date: 19-2-14 10:59
 * @version: 1.0
 * @description:
 */
public interface StudentRepository extends JpaRepository<StudentEntitys, String> {

    /**
     * 通过身份证号码查询有效状态的学生信息
     * @param isValidated 有效状态
     * @param idCardNo 身份证号码
     * @return
     */
    List<StudentEntitys> findByIsValidatedEqualsAndIdCardNo(String isValidated, String idCardNo);
}
