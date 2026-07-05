"""
对话记忆管理模块
基于 Redis 存储对话历史，支持会话级别的消息持久化和过期管理。
"""

import json
import logging
import os
import redis

logger = logging.getLogger(__name__)

# ============================================================
# Redis 连接配置：从环境变量读取连接参数
# ============================================================
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))

# 创建 Redis 连接客户端
_redis_client = redis.Redis(
    host=REDIS_HOST,
    port=REDIS_PORT,
    decode_responses=True,   # 自动将字节解码为字符串
    socket_connect_timeout=5,
)


def _session_key(session_id: str) -> str:
    """生成 Redis 中存储会话历史记录的 key。

    Args:
        session_id: 会话唯一标识符（通常由前端生成）

    Returns:
        Redis key 字符串，格式: chat:session:{session_id}
    """
    return f"chat:session:{session_id}"


def save_message(session_id: str, role: str, content: str) -> None:
    """追加一条消息到指定会话的对话历史中。

    消息以 JSON 格式追加到 Redis List 的末尾，
    并设置 30 分钟过期时间（每次追加后刷新 TTL）。
    Redis 写入失败时仅记录警告，不抛出异常。

    Args:
        session_id: 会话唯一标识符
        role: 消息角色，取值为 "user" / "assistant" / "tool"
        content: 消息文本内容
    """
    try:
        message = json.dumps({
            "role": role,
            "content": content,
        }, ensure_ascii=False)
        key = _session_key(session_id)
        # 使用管道批量执行，保证 RPUSH 和 EXPIRE 的原子性
        pipe = _redis_client.pipeline()
        pipe.rpush(key, message)
        pipe.expire(key, 1800)  # 30 分钟 = 1800 秒
        pipe.execute()
    except Exception as e:
        logger.warning(f"保存对话历史失败（不影响主流程）: {e}")


def get_history(session_id: str) -> list[dict]:
    """获取指定会话的最近 20 条对话历史。

    Args:
        session_id: 会话唯一标识符

    Returns:
        消息列表，每条消息为 {"role": str, "content": str} 字典，
        按时间从旧到新排列。如果会话不存在或已过期，返回空列表。
    """
    try:
        key = _session_key(session_id)
        # 获取 List 中最后 20 条记录（最新消息在末尾）
        raw_messages = _redis_client.lrange(key, -20, -1)
        if not raw_messages:
            return []
        # 将 JSON 字符串反序列化为字典列表
        return [json.loads(msg) for msg in raw_messages]
    except Exception as e:
        logger.warning(f"读取对话历史失败: {e}")
        return []


def clear_session(session_id: str) -> None:
    """清除指定会话的所有对话历史记录。

    Args:
        session_id: 会话唯一标识符
    """
    try:
        key = _session_key(session_id)
        _redis_client.delete(key)
    except Exception as e:
        logger.warning(f"清除会话失败: {e}")
