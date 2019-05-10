package com.forteach.wechat.mini.app.service.impl;

import cn.hutool.core.map.MapUtil;
import com.forteach.wechat.mini.app.domain.StudentEntitys;
import com.forteach.wechat.mini.app.repository.StudentRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private HashOperations<String, String ,String> hashOperations;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void saveUser() {
        List<StudentEntitys> list = new ArrayList<>();
        for (int i = 10001; i < 10101; i++) {
            StudentEntitys studentEntitys = new StudentEntitys();
            studentEntitys.setId(String.valueOf(i));
            studentEntitys.setIdCardNo(String.valueOf(i));
            studentEntitys.setUserName(String.valueOf(i));
            studentEntitys.setClassId(String.valueOf(i));
            studentEntitys.setPortrait("https://cdn.v2ex.com/gravatar/"+ DigestUtils.md5Hex(String.valueOf(i)) +".jpg?s=100&d=identicon");
            list.add(studentEntitys);
        }
        studentRepository.saveAll(list);
    }

    @Test
    public void saveRedis(){
        studentRepository.findAll().parallelStream()
                .filter(Objects::nonNull)
                .forEach(s->{
                    Map<String, String> map = MapUtil.newHashMap();
                    map.put("id", s.getId());
                    System.out.println("id ==>> "+ s.getId());
                    map.put("name", s.getUserName());
                    map.put("IDCardNO", s.getIdCardNo());
                    map.put("isValidated", "0");
                    map.put("portrait","https://cdn.v2ex.com/gravatar/"+ DigestUtils.md5Hex(s.getId().trim().toUpperCase()) +".jpg?s=100&d=identicon");
                    hashOperations.putAll("studentsData$".concat(s.getId()), map );

                        }
                );

        System.out.printf("保存完========>>>");
    }

}