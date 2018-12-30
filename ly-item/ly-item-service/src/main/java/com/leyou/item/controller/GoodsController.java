package com.leyou.item.controller;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import com.leyou.search.pojo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key){

        PageResult<SpuBo> pr = goodsService.querySpuByPage(page,rows,saleable,key);
        if (pr==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(pr);
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetail(@PathVariable("id") Long id){
        SpuDetail spuDetail = goodsService.querySpuDetail(id);
        return ResponseEntity.ok(spuDetail);
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySku(@RequestParam("id") Long id ){
        List<Sku> skus = goodsService.querySku(id);
        return ResponseEntity.ok(skus);
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updataGoods(@RequestBody SpuBo spuBo){
        goodsService.updataGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 查询根据SpuId查询SPU
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<SpuBo> querySpuById(@PathVariable("id") Long id){
        SpuBo spu = this.goodsService.querySpuById(id);
        if(spu == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }
    @GetMapping("skus")
    public ResponseEntity<List<Sku>> querySku(@RequestParam("skuIds") List<Long> ids ){
        List<Sku> skus = goodsService.querySkuList(ids);
        return ResponseEntity.ok(skus);
    }





}
