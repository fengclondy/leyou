package com.leyou.search.repository;

import com.leyou.item.pojo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.PageResult;
import com.leyou.search.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsService service;
    @Test
    public void textel(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);

    }

    @Test
    public  void  goodssave(){
        int page=1;
        int rows=100;
        int size=0;
        do {
        //查询SPU
        PageResult<SpuBo> spuBoPageResult = goodsClient.querySpuByPage(page, rows, true, null);
        List<SpuBo> items = spuBoPageResult.getItems();
        if (CollectionUtils.isEmpty(items)){
            break;
        }
        //构建Goods
        List<Goods> goods = items.stream().map(service::buildGoods).collect(Collectors.toList());
        //存入索引
        goodsRepository.saveAll(goods);
        page++;
        size=items.size();
        }while (size==100);
    }
}