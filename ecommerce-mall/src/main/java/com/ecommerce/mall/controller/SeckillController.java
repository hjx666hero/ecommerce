package com.ecommerce.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.dto.SeckillOrderDTO;
import com.ecommerce.mall.service.SeckillService;
import com.ecommerce.mall.vo.SeckillVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @GetMapping("/list")
    public Result<Page<SeckillVO>> getSeckillList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(seckillService.getSeckillList(page, size));
    }

    @GetMapping("/detail/{activityId}")
    public Result<SeckillVO> getSeckillDetail(@PathVariable Long activityId) {
        return Result.success(seckillService.getSeckillDetail(activityId));
    }

    @PostMapping("/order")
    public Result<String> createSeckillOrder(@Valid @RequestBody SeckillOrderDTO seckillOrderDTO,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(seckillService.createSeckillOrder(userId, seckillOrderDTO));
    }
}