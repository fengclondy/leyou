package com.leyou.controller;

import com.leyou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class PageController {


    @Autowired
    private PageService service;

    @GetMapping("{id}.html")
    public String page(@PathVariable("id") Long spuid, Model model){

        //数据封装到Map里
        Map<String,Object> map = service.saveMap(spuid);

        //填充到model
        model.addAllAttributes(map);
        //创建静态文件
        service.createHtml(spuid);

        //返回
        return "item";
    }
}
