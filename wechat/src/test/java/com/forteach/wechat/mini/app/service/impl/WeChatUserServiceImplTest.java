package com.forteach.wechat.mini.app.service.impl;

import com.forteach.wechat.mini.app.domain.StudentEntitys;
import com.forteach.wechat.mini.app.repository.StudentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 2019/2/16 19:56
 * @Version: 1.0
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class WeChatUserServiceImplTest {
    @Resource
    private StudentRepository studentRepository;

    @Test
    public void saveUser() {
        List<StudentEntitys> list = new ArrayList<>();
        for (int i = 10000; i < 10101; i++) {
            StudentEntitys studentEntitys = new StudentEntitys();
            studentEntitys.setId(String.valueOf(i));
            studentEntitys.setIdCardNo(String.valueOf(i));
            studentEntitys.setUserName(String.valueOf(i));
            list.add(studentEntitys);
        }
        studentRepository.saveAll(list);
    }
}