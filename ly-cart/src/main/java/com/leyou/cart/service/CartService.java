package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.filter.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.item.pojo.Sku;
import com.leyou.sms.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.collection.IsArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "ly:cart:uid:";

    public void addCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key =KEY_PREFIX+userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        String id = cart.getSkuId().toString();
        Integer num = cart.getNum();
        Boolean bool = hashOps.hasKey(id);
        if (bool){
            //如果商品已存在，就修改数量
            String o = hashOps.get(id).toString();
            Cart parse = JsonUtils.parse(o, Cart.class);
            parse.setNum(parse.getNum()+num);
        }else {
            //商品不存在，增加商品
            cart.setUserId(userInfo.getId());
            ArrayList<Long> list = new ArrayList<>();
            list.add(cart.getSkuId());
            //List<Sku> skus = goodsClient.querySku(list);
            List<Sku> skus = goodsClient.querySkus(list);
            System.out.println(skus.size());
            Sku sku = skus.get(0);
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
        }
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));


    }

    public List<Cart> queryCart() {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key =KEY_PREFIX+userInfo.getId();
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);
        List<Object> values = ops.values();
        // 判断是否有数据
        if(CollectionUtils.isEmpty(values)){
            return null;
        }
        // 查询购物车数据
        List<Cart> carts = values.stream().map(a -> JsonUtils.parse(a.toString(), Cart.class)).collect(Collectors.toList());
        return  carts;


    }

    public void updateNum(Cart carts) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key =KEY_PREFIX+userInfo.getId();
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);
        Object o = ops.get(carts.getSkuId().toString());
        Cart parse = JsonUtils.parse(o.toString(), Cart.class);
        parse.setNum(carts.getNum());
        ops.put(carts.getSkuId().toString(),JsonUtils.serialize(parse));
    }

    public void deleCart(String id) {
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        String key =KEY_PREFIX+userInfo.getId();
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(key);
        ops.delete(id);
    }
}
