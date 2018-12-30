package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecGroupService specGroupService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid") Long cid){
        List<SpecGroup> list = this.specGroupService.querySpecGroups(cid);
        if(list == null || list.size() == 0){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParam(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "search",required = false) Boolean search
    ){
        List<SpecParam> list = specGroupService.querySpecParam(gid,cid,search);
        if(list == null || list.size() == 0){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("group")
    public ResponseEntity<Void> addSpecGroup(@RequestBody SpecGroup sg){
        //System.out.println(sg);
        specGroupService.addSpecGroup(sg);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleSpecGroup(@PathVariable("id") Long id ){
        //System.out.println(sg);

        specGroupService.deleSpecGroup(id);

        return ResponseEntity.ok().build();
    }

    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroup sg){
        //System.out.println(sg);
        specGroupService.updateSpecGroup(sg);

        return ResponseEntity.ok().build();
    }
    @PostMapping("param")
    public ResponseEntity<Void> addSpecParam(@RequestBody SpecParam sp){
        //System.out.println(sg);
        specGroupService.addSpecParam(sp);

        return ResponseEntity.ok().build();
    }
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParam sp){
        //System.out.println(sg);
        specGroupService.updateSpecParam(sp);
        //specGroupService.addSpecParam(sp);

        return ResponseEntity.ok().build();
    }
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleSpecParam(@PathVariable("id") Long id){
        //System.out.println(sg);
        specGroupService.deleSpecParam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListGroup(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specGroupService.queryListGroup(cid));
    }







}