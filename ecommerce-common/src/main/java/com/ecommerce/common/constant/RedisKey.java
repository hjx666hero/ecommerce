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

    /** 分类树缓存 key: category:tree */
    public static final String CATEGORY_TREE = "category:tree";

    /** 分类树缓存过期时间 1小时 */
    public static final long CATEGORY_TREE_EXPIRE = 3600;

    /** 购物车数量上限 */
    public static final int CART_MAX_COUNT = 100;

    /** 首页数据缓存 key: home:data */
    public static final String HOME_DATA = "home:data";

    /** 首页数据缓存过期时间 10分钟 */
    public static final long HOME_DATA_EXPIRE = 600;

    /** 热搜词缓存 key: search:hot */
    public static final String SEARCH_HOT = "search:hot";

    /** 热搜词缓存过期时间 30分钟 */
    public static final long SEARCH_HOT_EXPIRE = 1800;

    /** 搜索历史 key: search:history:{userId} */
    public static final String SEARCH_HISTORY = "search:history:";

    private RedisKey() {}
}