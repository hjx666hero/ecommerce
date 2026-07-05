"""
工具定义模块
定义 Agent 可调用的业务函数及其 OpenAI Function Calling 格式的元数据。
"""

import os
import requests

# ============================================================
# 配置
# ============================================================
SPRING_BOOT_URL = os.getenv("SPRING_BOOT_URL", "http://localhost:8081")


# ============================================================
# 工具实现函数
# ============================================================

def query_order(order_id: str) -> str:
    """查询订单详情（含物流信息）。

    Args:
        order_id: 订单ID

    Returns:
        订单详情的 JSON 字符串，或错误提示
    """
    url = f"{SPRING_BOOT_URL}/api/internal/order/{order_id}"
    try:
        resp = requests.get(url, timeout=10, proxies={"http": None, "https": None})
        if resp.status_code == 404:
            return "未找到该订单"
        resp.raise_for_status()
        return resp.text
    except requests.exceptions.Timeout:
        return "查询订单超时，请稍后再试。"
    except requests.exceptions.RequestException as e:
        return f"查询订单失败：{str(e)}"


def create_ticket(user_id: str, title: str, description: str) -> str:
    """创建售后工单。

    Args:
        user_id: 用户ID
        title: 工单标题
        description: 工单详细描述

    Returns:
        工单创建结果的 JSON 字符串
    """
    url = f"{SPRING_BOOT_URL}/api/internal/ticket"
    payload = {"user_id": user_id, "title": title, "description": description}
    try:
        resp = requests.post(url, json=payload, timeout=10, proxies={"http": None, "https": None})
        resp.raise_for_status()
        return resp.text
    except requests.exceptions.Timeout:
        return "创建工单超时，请稍后再试。"
    except requests.exceptions.RequestException as e:
        return f"创建工单失败：{str(e)}"


# ============================================================
# OpenAI Function Calling 工具定义（元数据）
# ============================================================

TOOL_DEFINITIONS = [
    {
        "type": "function",
        "function": {
            "name": "query_order",
            "description": "根据订单ID查询用户的订单详情，包括订单状态、物流公司、物流单号等信息。当用户询问'我的订单到哪了'、'查物流'、'订单状态'等问题时调用此工具。",
            "parameters": {
                "type": "object",
                "properties": {
                    "order_id": {
                        "type": "string",
                        "description": "要查询的订单ID",
                    },
                },
                "required": ["order_id"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "create_ticket",
            "description": "为用户创建一个售后工单（退换货、投诉、维修等）。当用户需要'退货'、'换货'、'投诉'、'申请售后'时调用此工具。",
            "parameters": {
                "type": "object",
                "properties": {
                    "user_id": {
                        "type": "string",
                        "description": "用户ID",
                    },
                    "title": {
                        "type": "string",
                        "description": "工单标题，简要描述问题（如'物流未收到'、'商品质量问题'）",
                    },
                    "description": {
                        "type": "string",
                        "description": "工单详细描述，说明问题具体情况",
                    },
                },
                "required": ["user_id", "title", "description"],
            },
        },
    },
]


def get_tool_definitions():
    """返回 OpenAI Function Calling 格式的工具定义列表"""
    return TOOL_DEFINITIONS


def get_tool_map():
    """返回工具名称 → 函数映射"""
    return {
        "query_order": query_order,
        "create_ticket": create_ticket,
    }
