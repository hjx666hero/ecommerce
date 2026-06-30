package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.vo.MessageVO;

public interface MessageService {
    Page<MessageVO> getMessageList(Long userId, Integer page, Integer size);
    Integer getUnreadCount(Long userId);
    void markAsRead(Long userId, Long messageId);
    void markAllAsRead(Long userId);
}