package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class ProductDetailVo {
  private Integer  id;
  private Integer categoryId;
  private String name;
  private String subtitle;
  private String mainImage;
  private String subImages;
  private String detail;
  private BigDecimal price;
  private Integer stock;
  private Integer status;
  private Date createTime;
  private Date updateTime;

  
  private String imageHost;
  private Integer parentCategoryId;

}
