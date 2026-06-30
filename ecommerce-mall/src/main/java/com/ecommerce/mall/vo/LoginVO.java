package com.ecommerce.mall.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String nickname;
    private String avatar;
}