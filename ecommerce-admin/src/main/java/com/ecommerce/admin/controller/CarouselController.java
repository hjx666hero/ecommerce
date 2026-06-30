package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminCarouselService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.Carousel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/carousel")
@RequiredArgsConstructor
public class CarouselController {

    private final AdminCarouselService adminCarouselService;

    @GetMapping("/list")
    public Result<Page<Carousel>> getCarouselList(@RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminCarouselService.getCarouselList(page, size));
    }

    @PostMapping
    public Result<Void> saveCarousel(@RequestBody Carousel carousel) {
        adminCarouselService.saveCarousel(carousel);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCarousel(@PathVariable Long id) {
        adminCarouselService.deleteCarousel(id);
        return Result.success();
    }
}