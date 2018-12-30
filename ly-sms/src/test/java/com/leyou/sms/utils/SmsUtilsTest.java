package com.leyou.sms.utils;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsUtilsTest {

    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private AmqpTemplate template;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test() throws InterruptedException {
        //smsUtils.getRequest2("17621702282","123456");
        HashMap<String, String> map = new HashMap<>();
        map.put("phone","17621702282");
        map.put("code","123457");
        template.convertAndSend("ly.sms.exchange","sms.verify.code",map);
        Thread.sleep(1000);
    }




}
