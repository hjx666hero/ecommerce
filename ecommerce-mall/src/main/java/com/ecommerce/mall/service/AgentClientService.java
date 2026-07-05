package com.ecommerce.mall.service;

import com.ecommerce.mall.dto.AgentChatRequest;
import com.ecommerce.mall.dto.AgentChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Python Agent 服务调用客户端
 */
@Slf4j
@Service
public class AgentClientService {

    @Value("${agent.service.url:http://localhost:8000}")
    private String agentServiceUrl;

    private final RestTemplate restTemplate;

    public AgentClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AgentChatResponse chatWithAgent(String userId, String sessionId, String message) {
        String url = agentServiceUrl + "/api/chat";

        AgentChatRequest request = AgentChatRequest.builder()
                .user_id(userId)
                .session_id(sessionId)
                .message(message)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AgentChatRequest> entity = new HttpEntity<>(request, headers);

        try {
            AgentChatResponse response = restTemplate.postForObject(url, entity, AgentChatResponse.class);
            return response;
        } catch (RestClientException e) {
            log.error("调用Python Agent服务失败: url={}, error={}", url, e.getMessage());
            AgentChatResponse fallback = new AgentChatResponse();
            fallback.setReply("抱歉，智能客服暂时不可用，请稍后再试。如需紧急帮助，请联系人工客服。");
            fallback.setSuggestions(List.of("联系人工客服", "查看帮助中心"));
            return fallback;
        }
    }
}
