package com.leyou.item.pojo;

import java.util.List;

public class BrandBo extends Brand {

    private List<Long> cid;

    public List<Long> getCid() {
        return cid;
    }

    public void setCid(List<Long> cid) {
        this.cid = cid;
    }

    @Override
    public String toString() {
        return "BrandBo{" +
                "cid=" + cid +
                '}';
    }
}
