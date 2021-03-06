package com.leyou.item.controller;

import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import com.leyou.search.pojo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;


    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key) {
        PageResult<Brand> result = this.brandService.queryBrandByPageAndSort(page,rows,sortBy,desc, key);
        if (result == null || result.getItems().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }
    @PostMapping
    public  ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids){

        //System.out.println(brandBo);
        brandService.save(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandList(@PathVariable("cid") Long cid){

        List<Brand> brands = brandService.queryBrandList(cid);
        return ResponseEntity.ok(brands);

    }
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryById(@PathVariable Long id){
        Brand brand = brandService.queryByid(id);
        return  ResponseEntity.ok(brand);
    }
    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryByIds(@RequestParam("ids") List<Long> ids){
        List<Brand> brands =brandService.queryByIds(ids);
        return ResponseEntity.ok(brands);
    }
}
