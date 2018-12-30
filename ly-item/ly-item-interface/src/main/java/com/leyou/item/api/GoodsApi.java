package com.leyou.item.api;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.search.pojo.PageResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {
    @GetMapping("spu/detail/{id}")
    SpuDetail querySpuDetail(@PathVariable("id") Long id);

    @GetMapping("sku/list")
    List<Sku> querySku(@RequestParam("id") Long id );
    @GetMapping("spu/page")
    PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    @GetMapping("spu/{id}")
    SpuBo querySpuById(@PathVariable("id") Long id);

    @GetMapping("skus")
    List<Sku> querySkus(@RequestParam("skuIds") List<Long> ids );

}