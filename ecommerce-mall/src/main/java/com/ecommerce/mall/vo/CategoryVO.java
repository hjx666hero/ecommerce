package com.ecommerce.mall.vo;

import lombok.Data;
import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer sortOrder;
    private List<CategoryVO> children;
}