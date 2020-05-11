package com.changgou.goods.pojo;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {

    private Spu spu;
    private List<Sku> sku;

    public Goods() {
    }

    public Goods(Spu spu, List<Sku> sku) {
        this.spu = spu;
        this.sku = sku;
    }

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSku() {
        return sku;
    }

    public void setSku(List<Sku> sku) {
        this.sku = sku;
    }
}
