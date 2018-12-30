package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SpecGroupService {

    @Autowired
    protected SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroups(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);

        List<SpecGroup> groups = specGroupMapper.select(specGroup);
        return groups;
    }

    public List<SpecParam> querySpecParam(Long gid,Long cid,Boolean serach) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(serach);
        List<SpecParam> params = specParamMapper.select(specParam);
        return params;
    }

    @Transactional
    public void addSpecGroup(SpecGroup sg) {
        specGroupMapper.insertSelective(sg);
        //System.out.println(sg);

    }

    @Transactional
    public void deleSpecGroup(Long id) {
        SpecParam sp= new SpecParam();
        sp.setGroupId(id);
        List<SpecParam> select = specParamMapper.select(sp);
        if (select!=null||select.size()>0){
            specParamMapper.delete(sp);
        }
            specGroupMapper.deleteByPrimaryKey(id);


    }

    @Transactional
    public void updateSpecGroup(SpecGroup sg) {
        specGroupMapper.updateByPrimaryKeySelective(sg);
    }


    @Transactional
    public void addSpecParam(SpecParam sp) {
        specParamMapper.insertSelective(sp);
    }

    @Transactional
    public void updateSpecParam(SpecParam sp) {
        specParamMapper.updateByPrimaryKeySelective(sp);
    }

    @Transactional
    public void deleSpecParam(Long id) {
        specParamMapper.deleteByPrimaryKey(id);
    }

    public List<SpecGroup> queryListGroup(Long cid) {
        List<SpecGroup> Groups = querySpecGroups(cid);
        List<SpecParam> params = querySpecParam(null, cid, null);
        HashMap<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam param : params) {
            if(!map.containsKey(param.getGroupId())){
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        for (SpecGroup group : Groups) {
            group.setParams(map.get(group.getId()));
        }

        return Groups;
    }
}
