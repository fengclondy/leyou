package com.leyou.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(PageService.class);

    public Map<String, Object> saveMap(Long id) {
        try {
            // 模型数据
            Map<String, Object> modelMap = new HashMap<>();

            // 查询spu
            SpuBo spu = goodsClient.querySpuById(id);
            // 查询spuDetail
            SpuDetail detail = spu.getSpuDetail();
            //SpuDetail detail = this.goodsClient.querySpuDetailById(id);
            // 查询sku
            //List<Sku> skus = this.goodsClient.querySkuBySpuId(id);
            List<Sku> skus = spu.getSkus();

            // 装填模型数据
            modelMap.put("spu", spu);
            modelMap.put("spuDetail", detail);
            modelMap.put("skus", skus);

            // 准备商品分类
            List<Category> categories = getCategories(spu);
            if (categories != null) {
                modelMap.put("categories", categories);
            }

            // 准备品牌数据
            List<Brand> brands = this.brandClient.queryByIds(
                    Arrays.asList(spu.getBrandId()));
            modelMap.put("brand", brands.get(0));

            // 查询规格组及组内参数
            List<SpecGroup> groups = this.specificationClient.queryListGroup(spu.getCid3());
            modelMap.put("groups", groups);

            // 查询商品分类下的特有规格参数
            List<SpecParam> params =
                    this.specificationClient.querySpecParam(null, spu.getCid3(), null);
            // 处理成id:name格式的键值对
            Map<Long,String> paramMap = new HashMap<>();
            for (SpecParam param : params) {
                paramMap.put(param.getId(), param.getName());
            }
            modelMap.put("paramMap", paramMap);
            return modelMap;

        } catch (Exception e) {
            logger.error("加载商品数据出错,spuId:{}", id, e);
        }
        return null;
    }

    private List<Category> getCategories(Spu spu) {
        try {
            List<String> names = this.categoryClient.queryByids(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            Category c1 = new Category();
            c1.setName(names.get(0));
            c1.setId(spu.getCid1());

            Category c2 = new Category();
            c2.setName(names.get(1));
            c2.setId(spu.getCid2());

            Category c3 = new Category();
            c3.setName(names.get(2));
            c3.setId(spu.getCid3());

            return Arrays.asList(c1, c2, c3);
        } catch (Exception e) {
            logger.error("查询商品分类出错，spuId：{}", spu.getId(), e);
        }
        return null;
    }


    public void createHtml(Long sopId){
        PrintWriter writer = null;
        try {
        //模型数据
        Map<String, Object> map = saveMap(sopId);
        //上下文
        Context context= new Context();
        context.setVariables(map);
        //输出流
        File file=new File("E:\\test\\"+sopId+".html");
        //如果存在就删除
        if (file.exists()){
            file.delete();
        }

        writer = new PrintWriter(file,"UTF-8");
        templateEngine.process("item",context,writer);
        }catch (Exception e){
            logger.error("页面静态化出错：{}，"+ e, sopId);
        }finally {
            if (writer != null) {
                writer.close();
            }
        }


    }


    public void deleteHtml(Long id) {
        File file=new File("E:\\test\\"+id+".html");
        file.delete();
    }
}

