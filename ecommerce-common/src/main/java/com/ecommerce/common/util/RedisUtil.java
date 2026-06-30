package com.ecommerce.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== String 操作 ====================

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== Hash 操作 ====================

    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }

    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public Long hIncrement(String key, String hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    // ==================== Set 操作 ====================

    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ==================== ZSet 操作 ====================

    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // ==================== 分布式锁 ====================

    /**
     * 获取分布式锁 (SETNX + 过期时间)
     * @param key 锁的key
     * @param value 锁的值（用于释放时验证）
     * @param timeout 过期时间（秒）
     * @return 是否获取成功
     */
    public Boolean tryLock(String key, String value, long timeout) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁
     */
    public Boolean releaseLock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return result != null && result == 1;
    }

    // ==================== Lua脚本执行 ====================

    /**
     * 执行Lua脚本
     */
    public <T> T executeScript(String script, Class<T> resultType, List<String> keys, Object... args) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>(script, resultType);
        return redisTemplate.execute(redisScript, keys, args);
    }

    /**
     * 秒杀库存扣减 Lua脚本
     * KEYS[1]: 库存key
     * KEYS[2]: 用户限购key
     * ARGV[1]: 限购数量
     * 返回: -1 库存不足, -2 超出限购, >=0 扣减成功(剩余库存)
     */
    public static final String SECKILL_DECREASE_STOCK_SCRIPT =
            "local stock = tonumber(redis.call('get', KEYS[1]) or 0) " +
            "if stock <= 0 then return -1 end " +
            "local limit = tonumber(redis.call('get', KEYS[2]) or 0) " +
            "local limitPerUser = tonumber(ARGV[1]) " +
            "if limit >= limitPerUser then return -2 end " +
            "redis.call('decr', KEYS[1]) " +
            "redis.call('incr', KEYS[2]) " +
            "return redis.call('get', KEYS[1])";

    /**
     * 执行秒杀库存扣减
     */
    public Long seckillDecreaseStock(String stockKey, String limitKey, int limitPerUser) {
        return executeScript(SECKILL_DECREASE_STOCK_SCRIPT, Long.class,
                List.of(stockKey, limitKey), limitPerUser);
    }
}