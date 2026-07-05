package com.ecommerce.mall.task;

import com.ecommerce.mall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时预热秒杀库存到Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillStockPreheater implements ApplicationRunner {

    private final SeckillService seckillService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            seckillService.preheatSeckillStock();
            log.info("秒杀库存预热任务执行完成");
        } catch (Exception e) {
            log.error("秒杀库存预热失败: {}", e.getMessage());
        }
    }
}
