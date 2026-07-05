package com.ecommerce.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatResponse {
    private String reply;
    private List<String> suggestions;
}
