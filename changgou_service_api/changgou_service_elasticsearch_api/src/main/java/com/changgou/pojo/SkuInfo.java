package com.changgou.pojo;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Document(indexName = "skuinfo", type = "changgou")
public class SkuInfo implements Serializable {
    //创建elasticsearch索引库,以及映射


    /*
    * text类型：会分词，先把对象进行分词处理，然后再再存入到es中
    * keyword：不分词，没有把es中的对象进行分词处理，而是存入了整个对象！
    */



    /*
    * 1、ik_max_word
        会将文本做最细粒度的拆分，比如会将“中华人民共和国人民大会堂”拆分为“中华人民共和国、中华人民、中华、华人、人民共和国、人民、共和国、大会堂、大会、会堂等词语。
      2、ik_smart
        会做最粗粒度的拆分，比如会将“中华人民共和国人民大会堂”拆分为中华人民共和国、人民大会堂。
*/

    //主键
    @Id
    @Field(store = true, type = FieldType.Keyword, index = true)
    private Long id;

    //sku名字
    @Field(store = true, type = FieldType.Text, index = true, analyzer = "ik_smart")
    private String name;

    //价格
    @Field(store = true,type = FieldType.Double,index = true)
    private Integer price;

    //图片
    @Field(store = true,type = FieldType.Text,index = true)
    private String image;

    //创建时间
    @Field(type = FieldType.Date,index = true,store = true)
    private Date createTime;

    //更新时间
    @Field(type = FieldType.Date,index = true,store = true)
    private Date updateTime;

    //spuID
    @Field(type = FieldType.Keyword,index = true,store = true)
    private Long spuId;


    //规格列表
    @Field(store = true,type = FieldType.Keyword,index = true)
    private String spec;


    //品牌名称
    @Field(store = true,type = FieldType.Keyword,index = true)
    private String brandName;

    //分类id
    @Field(store = true,index = true,type = FieldType.Long)
    private Long categoryId;

    //分类名字
    @Field(store = true,index = true,type = FieldType.Keyword)
    private String categoryName;

    //销量
    @Field(index = true,type = FieldType.Integer,store = true)
    private Integer sale_num;

    //评论数
    @Field(store = true,index = true,type = FieldType.Integer)
    private Integer commentNum;

    //商品状态
    @Field(index = true,type = FieldType.Integer,store = true)
    private String status;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getSale_num() {
        return sale_num;
    }

    public void setSale_num(Integer sale_num) {
        this.sale_num = sale_num;
    }

    public Integer getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(Integer commentNum) {
        this.commentNum = commentNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
