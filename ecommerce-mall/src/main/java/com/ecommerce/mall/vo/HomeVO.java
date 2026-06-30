package com.ecommerce.mall.vo;

import lombok.Data;
import java.util.List;

@Data
public class HomeVO {
    private List<CarouselVO> carousels;
    private List<ProductVO> hotProducts;
    private List<ProductVO> newProducts;
    private List<CategoryVO> categories;
    private List<AnnouncementVO> announcements;
}