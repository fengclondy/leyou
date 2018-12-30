package com.leyou.search.client;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {

    @Autowired
    CategoryClient categoryClient;
    @Test
    public void queryByids() {
        List<String> strings = categoryClient.queryByids(Arrays.asList(1L, 2L, 3L));
        Assert.assertEquals(3,strings.size());
        String join = StringUtils.join(strings, " ");

        System.out.println(join);
    }
}