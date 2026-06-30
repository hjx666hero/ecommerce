package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminAnnouncementService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    @GetMapping("/list")
    public Result<Page<Announcement>> getAnnouncementList(@RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminAnnouncementService.getAnnouncementList(page, size));
    }

    @PostMapping
    public Result<Void> saveAnnouncement(@RequestBody Announcement announcement) {
        adminAnnouncementService.saveAnnouncement(announcement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        adminAnnouncementService.deleteAnnouncement(id);
        return Result.success();
    }
}