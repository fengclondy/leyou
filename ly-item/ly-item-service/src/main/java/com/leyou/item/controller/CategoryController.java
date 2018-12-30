package com.leyou.item.controller;

import com.github.pagehelper.PageInfo;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public ResponseEntity<List<Category>> findByPid(@RequestParam(value = "pid",defaultValue = "0") Long pid){

        PageInfo<Category> pageInfo = new PageInfo<>();
        List<Category> byPid = categoryService.findByPid(pid);
        if (byPid==null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(byPid);
    }

    @GetMapping("bid/")
    public ResponseEntity<List<Category>> findByBid(@RequestParam(value = "pid",defaultValue = "0") Long pid){

        PageInfo<Category> pageInfo = new PageInfo<>();
        List<Category> byPid = categoryService.findByPid(pid);
        if (byPid==null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(byPid);
    }

    @GetMapping("names")
    public ResponseEntity<List<String>> queryByids(@RequestParam("ids") List<Long> ids){
        List<String> list = categoryService.queryNamesByIds(ids);
        return ResponseEntity.ok(list);
    }


}
