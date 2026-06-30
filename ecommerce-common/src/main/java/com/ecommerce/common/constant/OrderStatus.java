package com.ecommerce.common.constant;

public class OrderStatus {

    /** 待付款 */
    public static final int PENDING_PAY = 0;

    /** 已支付 */
    public static final int PAID = 1;

    /** 已发货 */
    public static final int DELIVERED = 2;

    /** 已完成 */
    public static final int COMPLETED = 3;

    /** 已取消 */
    public static final int CANCELLED = 4;

    /** 已退款 */
    public static final int REFUNDED = 5;

    private OrderStatus() {}
}