package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.mall.entity.LocalMessage;
import com.ecommerce.mall.mapper.LocalMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalMessageService {

    private final LocalMessageMapper localMessageMapper;

    /**
     * 保存本地消息
     */
    public void saveMessage(String topic, String messageKey, String content) {
        LocalMessage msg = new LocalMessage();
        msg.setTopic(topic);
        msg.setMessageKey(messageKey);
        msg.setContent(content);
        msg.setStatus(0);
        msg.setRetryCount(0);
        msg.setMaxRetry(5);
        msg.setNextRetryTime(LocalDateTime.now().plusMinutes(1));
        localMessageMapper.insert(msg);
    }

    /**
     * 标记消息已处理
     */
    public void markCompleted(Long messageId) {
        LocalMessage msg = new LocalMessage();
        msg.setId(messageId);
        msg.setStatus(2);
        localMessageMapper.updateById(msg);
    }

    /**
     * 标记消息失败
     */
    public void markFailed(Long messageId, String errorMsg) {
        LocalMessage msg = new LocalMessage();
        msg.setId(messageId);
        msg.setStatus(3);
        msg.setErrorMsg(errorMsg);
        localMessageMapper.updateById(msg);
    }
}
