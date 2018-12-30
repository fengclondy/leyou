package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuDateilMapper spuDateilMapper;
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuBo> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {

        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        if (saleable !=null) {
            criteria.orEqualTo("saleable", saleable);
        }

        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        List<Spu> spuss = spuMapper.selectByExample(example);
        //Page<Spu> pageInfo = (Page<Spu>) this.spuMapper.selectByExample(example);

        PageInfo pageInfo = new PageInfo(spuss);

        List<SpuBo> list = spuss.stream().map(spu -> {
            // 把spu变为 spuBo
            SpuBo spuBo = new SpuBo();
            // 属性拷贝
            BeanUtils.copyProperties(spu, spuBo);

            // 2、查询spu的商品分类名称,要查三级分类
            List<String> names = this.categoryService.queryNamesByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            // 将分类名称拼接后存入
            spuBo.setCname(StringUtils.join(names, "/"));

            // 3、查询spu的品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            return spuBo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(), list);
    }


    @Transactional
    public void saveGoods(SpuBo spu) {
        //保存SPU

        //spu.setSaleable(true);
        //spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        this.spuMapper.insertSelective(spu);
        //保存SPU详情
        spu.getSpuDetail().setSpuId(spu.getId());
        this.spuDateilMapper.insert(spu.getSpuDetail());

        // 保存sku和库存信息
        saveSkuAndStock(spu.getSkus(), spu.getId());

        //发送消息
        amqpTemplate.convertAndSend("ly.item.exchange","item..insert",spu.getId());


    }

    @Transactional
    public void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuId);
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    public SpuDetail querySpuDetail(Long id) {
        SpuDetail spuDetail = spuDateilMapper.selectByPrimaryKey(id);
        if (spuDetail==null){
            return null;
        }
        return spuDetail;
    }

    public  List<Sku> querySku(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> select = skuMapper.select(sku);
        for (Sku sku1 : select) {
            sku1.setStock(stockMapper.selectByPrimaryKey(sku1.getId()).getStock());
        }
        return select;
    }

    @Transactional
    public void updataGoods(SpuBo spuBo) {

        List<Sku> skus = querySku(spuBo.getId());

        if (skus.size()>0||skus!=null){
            List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
            // 删除以前库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);

            // 删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spuBo.getId());
            this.skuMapper.delete(record);
        }
        // 新增sku和库存
        saveSkuAndStock(spuBo.getSkus(), spuBo.getId());

        // 更新spu
        spuBo.setLastUpdateTime(new Date());
        //spuBo.setCreateTime(null);
        //spuBo.setValid(null);
        //spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        // 更新spu详情

        this.spuDateilMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //发送消息
        amqpTemplate.convertAndSend("ly.item.exchange","item..update",spuBo.getId());

    }

    public SpuBo querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //List<Sku> skus = querySku(id);
        //SpuDetail spuDetail = querySpuDetail(id);
        SpuBo spuBo = new SpuBo();
        spuBo.setSkus(querySku(id));
        spuBo.setSpuDetail(querySpuDetail(id));
        spuBo.setSubTitle(spu.getSubTitle());
        spuBo.setTitle(spu.getTitle());
        spuBo.setId(spu.getId());
        spuBo.setBrandId(spu.getBrandId());
        spuBo.setCid1(spu.getCid1());
        spuBo.setCid2(spu.getCid2());
        spuBo.setCid3(spu.getCid3());
        return spuBo;
    }

    public List<Sku> querySkuList(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        for (Sku sku1 : skus) {
            sku1.setStock(stockMapper.selectByPrimaryKey(sku1.getId()).getStock());
        }
        return skus;
    }
}

