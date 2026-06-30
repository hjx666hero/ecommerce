package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.Announcement;
import com.ecommerce.mall.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public Page<Announcement> getAnnouncementList(Integer page, Integer size) {
        return announcementMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime));
    }

    @Transactional
    public void saveAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            announcementMapper.insert(announcement);
        } else {
            announcementMapper.updateById(announcement);
        }
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }
}