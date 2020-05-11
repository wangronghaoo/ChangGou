package com.changgou.goods.service.impl;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    //分布式id
    @Autowired
    private IdWorker idWorker;


    @Autowired
    private SkuMapper skuMapper;


    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param spu_id
     * @return
     */
    @Override
    public Goods findById(String spu_id) {
            //查询spu
            Spu spu = spuMapper.selectByPrimaryKey(spu_id);
            //根据spu_id查询sku列表
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            //对应的属性,以及字段
            criteria.andEqualTo("spuId", spu.getId());

            List<Sku> skus = skuMapper.selectByExample(example);
            return new Goods(spu, skus);

    }

    /**
     * 增加
     * @param goods
     */
    @Override
    public void add(Goods goods){

        //添加spu表
        //获取spu
        Spu spu = goods.getSpu();

        //将分类ID与SPU的品牌ID 一起插入到tb_category_brand表中

        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0) {
            //没有重复添加
            categoryBrandMapper.insert(categoryBrand);
        }


        //添加sku集合
        saveSkuList(goods);

    }


    /**
     * 修改
     * @param goods
     * @param id
     */
    @Override
    public void update(Goods goods, String id){
        //修改spu
        Spu spu = goods.getSpu();
        spu.setId(id);
        spuMapper.updateByPrimaryKey(goods.getSpu());
        //删除原先的sku
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId",id);
        skuMapper.deleteByExample(example);
        //添加新的sku
        saveSkuList(goods);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        spuMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    /**
     * 商品上架
     * @param spu_id
     * @return
     */
    @Override
    public Result putAway(String spu_id) {

        //先根据spu_id查询出整个spu
        Spu spu = spuMapper.selectByPrimaryKey(spu_id);
        if (spu == null){
            return new Result(false,StatusCode.ERROR,"商品不存在");
        }
        //设置审核状态为1
        spu.setStatus("1");
        spuMapper.updateByPrimaryKeySelective(spu);

        if ("1".equals(spu.getIsDelete())){
            return new Result(false, StatusCode.ERROR,"该商品已经被删除,无法上架.");
        }
        if ("0".equals(spu.getStatus())){
            return new Result(false,StatusCode.ERROR,"该商品未完成审核,请稍后再试!!!");
        }
        if ("1".equals(spu.getIsMarketable())){
            return new Result(false,StatusCode.ERROR,"该商品已经上架,无法再次上架");
        }
        //修改isMarketable为1
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        return new Result(true,StatusCode.OK,"商品上架成功.");
    }

    /*
    下架商品
     */
    @Override
    public Result pull(String spuId) {
        //商品已经存在,直接修改状态
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
        return new Result(true,StatusCode.OK,"商品下架成功.");
    }

    /**
     * 逻辑删除商品
     * @param id
     * @return
     */
    @Override
    public Result falseDelete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //必须先下架才能删除
        if ("1".equals(spu.getIsMarketable())){
            return new Result(false,StatusCode.ERROR,"请先下架商品,才能进行删除.");
        }
        spu.setIsDelete("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        return new Result(true,StatusCode.OK,"商品删除成功.");
    }


    /**
     * 回收站商品还原
     * @param id
     */
    @Override
    public void backup(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //还原之后,审核状态为未完成审核
        spu.setStatus("0");
        spu.setIsDelete("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public Spu findSpuById(String spuId) {
        return spuMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }


    //保存skuList
    private void saveSkuList(Goods goods){
        Spu spu = goods.getSpu();
        //添加spu
        //设置分布式id
        String spu_Id = String.valueOf(idWorker.nextId());
        spu.setId(spu_Id);
        //是否上架
        spu.setIsMarketable("0");
        //是否删除
        spu.setIsDelete("0");
        //审核状态
        spu.setStatus("0");
        spuMapper.insertSelective(spu);
        //添加sku表
        List<Sku> skuList = goods.getSku();
        if (skuList != null && !"".equals(skuList)){
            //遍历
            for (Sku sku : skuList) {
                sku.setId(String.valueOf(idWorker.nextId()));
                //设置创建时间
                sku.setCreateTime(new Date());
                //根据brandId查询品牌id
                Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
                //设置品牌名称
                sku.setBrandName(brand.getName());
                //设置spuId
                sku.setSpuId(String.valueOf(spu.getId()));
                Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
                //设置种类名称,以及id
                sku.setCategoryId(category.getId());
                sku.setCategoryName(category.getName());
                sku.setStatus("1");
                //设置sku名称  spu + 规格
                String name = spu.getName();
                String spec = sku.getSpec();
                if (spec == null || "".equals(spec)){
                    sku.setSpec("{}");
                }
                sku.setName(spec + name);

                skuMapper.insertSelective(sku);
            }
        }
    }

}
