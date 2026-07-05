package com.ecommerce.mall.controller;

import com.ecommerce.common.result.Result;
import com.ecommerce.mall.dto.AgentChatResponse;
import com.ecommerce.mall.dto.ChatRequestDTO;
import com.ecommerce.mall.dto.ChatResponseDTO;
import com.ecommerce.mall.service.AgentClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 智能客服控制器 - 前端入口
 */
@RestController
@RequestMapping("/api/v1/customer-service")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final AgentClientService agentClientService;

    @PostMapping("/chat")
    public Result<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        // 从 header 中获取或生成 session_id
        String sessionId = httpRequest.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        AgentChatResponse agentResponse = agentClientService.chatWithAgent(
                userId.toString(), sessionId, request.getMessage());

        ChatResponseDTO response = new ChatResponseDTO();
        response.setReply(agentResponse.getReply());
        response.setSessionId(sessionId);
        response.setSuggestions(agentResponse.getSuggestions());

        return Result.success(response);
    }
}
