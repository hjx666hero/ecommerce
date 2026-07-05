package com.ecommerce.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 1xxx
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ACCOUNT_DISABLED(1003, "账号已被禁用"),
    USER_PHONE_EXIST(1004, "手机号已注册"),
    USER_EMAIL_EXIST(1005, "邮箱已注册"),
    VERIFY_CODE_ERROR(1006, "验证码错误"),
    VERIFY_CODE_EXPIRED(1007, "验证码已过期"),
    TOKEN_EXPIRED(1008, "Token已过期"),
    TOKEN_INVALID(1009, "Token无效"),
    REFRESH_TOKEN_EXPIRED(1010, "Refresh Token已过期"),

    // 商品模块 2xxx
    PRODUCT_NOT_EXIST(2001, "商品不存在"),
    PRODUCT_OFF_SHELF(2002, "商品已下架"),
    STOCK_NOT_ENOUGH(2003, "库存不足"),
    SKU_NOT_EXIST(2004, "SKU不存在"),

    // 订单模块 3xxx
    ORDER_NOT_EXIST(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态不正确"),
    ORDER_CANNOT_CANCEL(3003, "订单无法取消"),
    ORDER_DUPLICATE_SUBMIT(3004, "请勿重复提交订单"),
    ORDER_CREATE_FAIL(3005, "订单创建失败"),
    ORDER_NOT_PURCHASED(3006, "您尚未购买该商品，无法评价"),

    // 支付模块 4xxx
    PAY_FAIL(4001, "支付失败"),
    PAY_RECORD_NOT_EXIST(4002, "支付记录不存在"),
    REFUND_FAIL(4003, "退款失败"),

    // 优惠券模块 5xxx
    COUPON_NOT_EXIST(5001, "优惠券不存在"),
    COUPON_RECEIVED(5002, "已领取过该优惠券"),
    COUPON_EXHAUSTED(5003, "优惠券已领完"),
    COUPON_EXPIRED(5004, "优惠券已过期"),
    COUPON_NOT_AVAILABLE(5005, "优惠券不可用"),
    COUPON_NOT_MEET_AMOUNT(5006, "未达到优惠券使用门槛"),

    // 秒杀模块 6xxx
    SECKILL_NOT_EXIST(6001, "秒杀活动不存在"),
    SECKILL_NOT_START(6002, "秒杀活动未开始"),
    SECKILL_ENDED(6003, "秒杀活动已结束"),
    SECKILL_STOCK_EMPTY(6004, "秒杀库存不足"),
    SECKILL_LIMIT(6005, "超出秒杀限购数量"),
    SECKILL_FREQUENT(6006, "操作过于频繁"),

    // 地址模块 7xxx
    ADDRESS_NOT_EXIST(7001, "地址不存在"),
    ADDRESS_LIMIT(7002, "最多只能添加20个地址"),

    // 管理后台 8xxx
    ADMIN_NOT_EXIST(8001, "管理员不存在"),
    ADMIN_PASSWORD_ERROR(8002, "管理员密码错误"),
    ADMIN_DISABLED(8003, "管理员已被禁用"),

    // 购物车 9xxx
    CART_ITEM_NOT_EXIST(9001, "购物车商品不存在"),
    ORDER_CANNOT_DELETE(9002, "订单无法删除");

    private final int code;
    private final String message;
}