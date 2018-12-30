package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.item.mapper.BrandMapper;

import com.leyou.item.pojo.Brand;

import com.leyou.search.pojo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    protected BrandMapper brandMapper;



    public PageResult<Brand> queryBrandByPageAndSort(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        // 开始分页

        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            String st = "%"+key+"%";
            criteria.andLike("name",st).orEqualTo("letter", key.toUpperCase());
        }
        if (StringUtils.isNotBlank(sortBy)) {
            // 排序
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        // 查询
        List<Brand> brands = brandMapper.selectByExample(example);
        //System.out.println(brands.get(0));
       // List<Brand> categories = brandMapper.selectByExample(example);
        //Page<Brand> pageInfo = (Page<Brand>) brandMapper.selectByExample(example);
        PageInfo pageInfo = new PageInfo(brands);

        // 返回结果
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    @Transactional
    public void save(Brand brand, List<Long> cids) {


        brandMapper.insertSelective(brand);


        for (Long ci : cids) {
            brandMapper.saveBrandCategory(brand.getId(),ci);
        }

    }

    public List<Brand> queryBrandList(Long cid) {
        return brandMapper.queryBrandList(cid);
    }

    public Brand queryByid(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> list = brandMapper.selectByIdList(ids);
        return list;
    }
}
