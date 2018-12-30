package com.leyou.user.service;

import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.sms.utils.NumberUtils;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper mapper;

    @Autowired
    private AmqpTemplate template;

    @Autowired
    private StringRedisTemplate redisTem;

    static final String KEY_PREFIX = "user:code:phone:";

    static final Logger logger = LoggerFactory.getLogger(UserService.class);


    public Boolean check(String data, Integer type) {
        User user = new User();

        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
             default:
                 return false;
        }

        int i = mapper.selectCount(user);
        return i==0;
    }

    public void sms(String phone) {
        String s = NumberUtils.generateCode(6);
        try {
        Map<String,String> map = new HashMap();
        map.put("phone",phone);
        map.put("code",s);
        template.convertAndSend("ly.sms.exchange","sms.verify.code",map);
        redisTem.delete(KEY_PREFIX+phone);
        redisTem.opsForValue().set(KEY_PREFIX+phone,s,10, TimeUnit.MINUTES);
        return;
        }catch (Exception e){
            logger.error("发送短信失败。phone：{}， code：{}", phone, s);
        }
    }


    public void register(User user, String code) {
    //判断验证码
        String s = redisTem.opsForValue().get(KEY_PREFIX + user.getPhone());
        System.out.println(s);
        if (!StringUtils.equals(s,code)) {
            throw new RuntimeException("验证码错误");
        }

        user.setCreated(new Date());

        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        mapper.insert(user);
        redisTem.delete(KEY_PREFIX + user.getPhone());
    }

    public User queryUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        User user1 = mapper.selectOne(user);
        if (StringUtils.isAllEmpty(user1.getUsername())) {
            throw new RuntimeException("账户和密码错误");
        }
        if (!StringUtils.equals(CodecUtils.md5Hex(password,user1.getSalt()),user1.getPassword())) {
            throw new RuntimeException("账户和密码错误");
        }
        return  user1;
    }
}
