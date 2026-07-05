package com.ecommerce.mall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String reply;
    private String sessionId;
    private List<String> suggestions;
}
