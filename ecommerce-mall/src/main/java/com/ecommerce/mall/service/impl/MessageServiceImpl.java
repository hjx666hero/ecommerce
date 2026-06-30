package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.RedisKey;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.mall.entity.Message;
import com.ecommerce.mall.mapper.MessageMapper;
import com.ecommerce.mall.service.MessageService;
import com.ecommerce.mall.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final RedisUtil redisUtil;

    @Override
    public Page<MessageVO> getMessageList(Long userId, Integer page, Integer size) {
        Page<Message> messagePage = messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, userId)
                        .orderByDesc(Message::getCreateTime));
        List<MessageVO> voList = messagePage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        Page<MessageVO> result = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        String key = RedisKey.MESSAGE_UNREAD + userId;
        Object count = redisUtil.get(key);
        if (count != null) {
            return Integer.parseInt(count.toString());
        }
        Long dbCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId).eq(Message::getIsRead, 0));
        redisUtil.set(key, dbCount.intValue());
        return dbCount.intValue();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null && message.getUserId().equals(userId) && message.getIsRead() == 0) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            messageMapper.updateById(message);
            // 更新Redis未读数
            String key = RedisKey.MESSAGE_UNREAD + userId;
            redisUtil.decrement(key, 1);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1)
                .set(Message::getReadTime, LocalDateTime.now()));
        // 清除Redis未读数
        String key = RedisKey.MESSAGE_UNREAD + userId;
        redisUtil.set(key, 0);
    }

    private MessageVO toVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setTypeText(getTypeText(message.getType()));
        vo.setIsRead(message.getIsRead());
        vo.setReadTime(message.getReadTime());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private String getTypeText(Integer type) {
        switch (type) {
            case 1: return "系统消息";
            case 2: return "订单消息";
            case 3: return "促销消息";
            default: return "未知";
        }
    }
}