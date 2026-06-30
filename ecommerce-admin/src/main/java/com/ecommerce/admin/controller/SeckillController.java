package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminSeckillService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.SeckillActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final AdminSeckillService adminSeckillService;

    @GetMapping("/list")
    public Result<Page<SeckillActivity>> getSeckillList(@RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminSeckillService.getSeckillList(page, size));
    }

    @PostMapping
    public Result<Void> saveSeckill(@RequestBody SeckillActivity activity) {
        adminSeckillService.saveSeckill(activity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSeckill(@PathVariable Long id) {
        adminSeckillService.deleteSeckill(id);
        return Result.success();
    }
}