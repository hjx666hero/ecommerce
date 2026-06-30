package com.ecommerce.common.constant;

public class RedisKey {

    /** 验证码 key: verify:code:{phone} */
    public static final String VERIFY_CODE = "verify:code:";

    /** 验证码有效期 5分钟 */
    public static final long VERIFY_CODE_EXPIRE = 300;

    /** 用户Token key: token:user:{userId} */
    public static final String USER_TOKEN = "token:user:";

    /** Access Token有效期 2小时 */
    public static final long ACCESS_TOKEN_EXPIRE = 7200;

    /** Refresh Token有效期 7天 */
    public static final long REFRESH_TOKEN_EXPIRE = 604800;

    /** 秒杀库存 key: seckill:stock:{activityId} */
    public static final String SECKILL_STOCK = "seckill:stock:";

    /** 秒杀用户限购 key: seckill:limit:{activityId}:{userId} */
    public static final String SECKILL_LIMIT = "seckill:limit:";

    /** 分布式锁 key: lock:order:{userId} */
    public static final String LOCK_ORDER = "lock:order:";

    /** 分布式锁 key: lock:coupon:{couponId}:{userId} */
    public static final String LOCK_COUPON = "lock:coupon:";

    /** 用户未读消息数 key: message:unread:{userId} */
    public static final String MESSAGE_UNREAD = "message:unread:";

    /** 商品收藏 key: favorite:{userId} */
    public static final String USER_FAVORITE = "favorite:";

    /** 管理员Token */
    public static final String ADMIN_TOKEN = "token:admin:";

    /** 秒杀下单队列 */
    public static final String SECKILL_ORDER_QUEUE = "seckill:order:queue";

    private RedisKey() {}
}