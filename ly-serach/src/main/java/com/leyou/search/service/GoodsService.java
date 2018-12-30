package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.PageResult;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.sms.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    BrandClient brandClient;
    @Autowired
    CategoryClient categoryClient;
    @Autowired
    GoodsClient goodsClient;
    @Autowired
    GoodsRepository repository;
    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private SpecificationClient specificationClient;

    public Goods buildGoods(Spu spu){
        Long spuId = spu.getId();
        //查询分类
        List<String> list = categoryClient.queryByids(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        //all
        String all =spu.getSubTitle()+StringUtils.join(list," ")+brand.getName();
        //查询SKU
        List<Sku> skus = goodsClient.querySku(spuId);
        //对SKU处理
        List<Long> collect = new ArrayList<>();
        List<Map<String,Object>> skulist = new ArrayList<>();
        for (Sku sku : skus) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("images",StringUtils.substringBefore(sku.getImages(),",") );
            skulist.add(map);
            collect.add(sku.getPrice());
        }
        //查询规格参数
        List<SpecParam> specParams = specificationClient.querySpecParam(null, spu.getCid3(),true);
        if (specParams==null||specParams.size()==0){
            throw new RuntimeException("没有查询到");
        }
        //查询商品详情

        SpuDetail spuDetail = goodsClient.querySpuDetail(spuId);
        //通用参数转mao集合
        Map<Long, String> longStringMap = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //特有参数转map集合
        String specialSpec = spuDetail.getSpecialSpec();
        Map<Long, List<String>> longListMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : specParams) {
            String key = param.getName();
            Object vlue = null;
            if (param.getGeneric()){
               vlue = longStringMap.get(param.getId());
               if (param.getNumeric()){
                   vlue = chooseSegment(vlue.toString(),param);
               }
            }else {
                vlue = longListMap.get(param.getId());
            }
            specs.put(key,vlue);
        }


        //构建GOODS对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());  //设置品牌ID
        goods.setCreateTime(spu.getCreateTime());  //设置创建时间
        goods.setSubTitle(spu.getSubTitle()); //设置小标题
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());//设置分类ID
        goods.setId(spuId);//设置ID
        goods.setAll(all); //所有需要被搜索的信息，包含标题，分类，甚至品牌
        goods.setPrice(collect); // 价格集合
        goods.setSkus(JsonUtils.serialize(skulist));  //  Sku集合  Json格式
        goods.setSpecs(specs);   //  规格参数集合



        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest searchRequest) {
        //分页
        Integer page = searchRequest.getPage()-1;
        Integer size = searchRequest.getSize();
        //构建查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //过滤
        queryBuilder.withPageable(PageRequest.of(page,size));
        //聚合查询
        String category_name="category_name";
        queryBuilder.addAggregation(AggregationBuilders.terms(category_name).field("cid3"));
        String brand_name="brand_name";
        queryBuilder.addAggregation(AggregationBuilders.terms(brand_name).field("brandId"));
        QueryBuilder baseQuery = basicQueryBuilder(searchRequest);
        queryBuilder.withQuery(baseQuery);
        //执行查询

        AggregatedPage<Goods> goods = template.queryForPage(queryBuilder.build(), Goods.class);
        //Page<Goods> goods = repository.search(queryBuilder.build());
        Aggregations aggregations = goods.getAggregations();

        List<Category> categoryList=getCategoryList(aggregations.get(category_name));
        List<Brand> brandList=getBrandList(aggregations.get(brand_name));
        //判断分类集合是否为1,为一的话就进行规格参数聚合
        List<Map<String,Object>> spec=null;
        if(categoryList.size()==1 && categoryList!=null){
        spec = getspec(baseQuery,categoryList.get(0));
        }

        Integer totalPages = goods.getTotalPages();
        long l = totalPages.longValue();
        long totalElements = goods.getTotalElements();
        long totalPage = (totalElements + size - 1) / size;
        //List<Goods> content = goods.getContent();
        return new SearchResult(totalElements, totalPage, goods.getContent(),categoryList,brandList,spec);
    }

    private QueryBuilder basicQueryBuilder(SearchRequest searchRequest) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        //构建基本查询条件
        builder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()));
        //过滤
        Map<String, String> filter = searchRequest.getFilter();
        //遍历Map集合添加过滤条件
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //整理key
            if (!"cid3".equals(key) && !"brandId".equals(key)){
                key="specs." + key + ".keyword";
            }
            builder.filter(QueryBuilders.termQuery(key,value));
        }
        return builder;
    }

    /**
     * 查询规格参数集合
     * @param baseQuery
     * @param category
     * @return
     */
    private List<Map<String,Object>> getspec(QueryBuilder baseQuery, Category category) {
        List<Map<String, Object>> maps = new ArrayList<>();
        //查询需要聚合的参数
        List<SpecParam> params = specificationClient.querySpecParam(null, category.getId(), true);
        //创建聚合构建器
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        //把之前的查询条件带上
        queryBuilder.withQuery(baseQuery);
        //聚合
        for (SpecParam param : params) {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        }
        //获取结果
        AggregatedPage<Goods> goods = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = goods.getAggregations();
        //解析并返回
        for (SpecParam param : params) {
            StringTerms aggregation = aggregations.get(param.getName());
            Map<String, Object> map = new HashMap<>();
            map.put("k",param.getName());
            map.put("options",aggregation.getBuckets().stream().map(s -> s.getKeyAsString()).collect(Collectors.toList()));
            maps.add(map);
        }

        return maps;


    }

    /**
     * 转换成brand集合
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandList(LongTerms aggregation) {

        try {
        //获取所有category的ID
        List<Long> brandids = aggregation.getBuckets().stream().map(s -> s.getKeyAsNumber().longValue()).collect(Collectors.toList());
        //获取所有brand
        List<Brand> list = brandClient.queryByIds(brandids);
        return  list;

        }catch (Exception e){
            System.out.println("没有查询到品牌");
            return null;
        }

    }


    /**
     * 转换成category集合
     * @param aggregation
     * @return
     */
    private List<Category> getCategoryList(LongTerms aggregation) {
        try {
        //获取所有category的ID
        List<Long> cids = aggregation.getBuckets().stream().map(s -> s.getKeyAsNumber().longValue()).collect(Collectors.toList());
        //获取所有category的name
        List<String> names = categoryClient.queryByids(cids);
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Category c = new Category();
            c.setId(cids.get(i));
            c.setName(names.get(i));
            categories.add(c);
        }
        return categories;
        }catch (Exception e){
            System.out.println("没有查询到分类");
            return null;
        }
    }

    public void createIndex(Long id){
        SpuBo spuBo = goodsClient.querySpuById(id);
        Goods goods = buildGoods(spuBo);
        repository.save(goods);
    }

    public void deleteIndex(Long id) {
        repository.deleteById(id);
    }
}
