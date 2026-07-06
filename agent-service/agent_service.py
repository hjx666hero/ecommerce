"""
核心 Agent 逻辑模块
使用原生 HTTP 请求调用大模型 API（requests 库，走系统代理）。
"""

import os
import json
import logging
import requests
import memory
import tools

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

MODEL_NAME = os.getenv("MODEL_NAME", "gpt-4o-mini")
OPENAI_BASE_URL = os.getenv("OPENAI_BASE_URL", "https://api.openai.com/v1")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

SYSTEM_PROMPT = (
    "你是云创优品电商平台的智能客服助手。"
    "你可以帮助用户查询订单物流状态、创建售后工单等。"
    "请用友好、专业的中文回复用户。"
    "如果用户没有提供足够的信息（如订单ID），请礼貌地询问。"
)

CHAT_URL = f"{OPENAI_BASE_URL.rstrip('/')}/chat/completions"


def _call_llm(messages: list, tools_defs: list = None) -> dict:
    """调用大模型 API"""
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {OPENAI_API_KEY}",
    }
    payload = {
        "model": MODEL_NAME,
        "messages": messages,
        "temperature": 0.3,
    }
    if tools_defs:
        payload["tools"] = tools_defs

    # 显式禁用代理，避免残留的 HTTP_PROXY 环境变量干扰
    resp = requests.post(
        CHAT_URL, json=payload, headers=headers, timeout=60,
        proxies={"http": None, "https": None}
    )
    resp.raise_for_status()
    return resp.json()


def _call_llm_text(messages: list) -> str:
    """调用大模型获取纯文本回复"""
    data = _call_llm(messages)
    choice = data["choices"][0]
    return choice["message"].get("content", "") or ""


def process_message(user_id: str, session_id: str, message: str) -> dict:
    history = memory.get_history(session_id)

    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    messages.extend(history)
    messages.append({"role": "user", "content": message})

    tool_defs = tools.get_tool_definitions()
    tool_map = tools.get_tool_map()

    try:
        data = _call_llm(messages, tools_defs=tool_defs if tool_defs else None)
        choice = data["choices"][0]
        reply = ""

        if choice["finish_reason"] == "tool_calls":
            # 记录 assistant 的 tool_calls
            tool_call_msgs = choice["message"]["tool_calls"]
            messages.append({
                "role": "assistant",
                "content": None,
                "tool_calls": tool_call_msgs,
            })

            # 执行所有工具调用
            for tc in tool_call_msgs:
                func_name = tc["function"]["name"]
                func_args = json.loads(tc["function"]["arguments"])

                if "user_id" in func_args and func_args["user_id"] is None:
                    func_args["user_id"] = user_id

                logger.info(f"调用工具: {func_name}, 参数: {func_args}")
                fn = tool_map.get(func_name)
                result = fn(**func_args) if fn else f"未知工具: {func_name}"

                messages.append({
                    "role": "tool",
                    "tool_call_id": tc["id"],
                    "content": result,
                })

            # 第二次调用获取最终回复
            reply = _call_llm_text(messages)
        else:
            reply = choice["message"].get("content", "") or ""

        memory.save_message(session_id, "user", message)
        memory.save_message(session_id, "assistant", reply)

        suggestions = _generate_suggestions(message, reply)
        logger.info(f"用户 {user_id} 会话 {session_id}: 完成")
        return {"reply": reply, "suggestions": suggestions}

    except Exception as e:
        import traceback
        traceback.print_exc()
        logger.error(f"处理失败: {e}")
        return {
            "reply": "抱歉，处理您的问题时出现了错误，请稍后再试或联系人工客服。",
            "suggestions": ["联系人工客服"],
        }


def _generate_suggestions(user_message: str, reply: str) -> list:
    msg_lower = user_message.lower()
    if any(kw in msg_lower for kw in ["订单", "物流", "快递", "发货", "到哪"]):
        return ["查询其他订单", "申请售后"]
    elif any(kw in msg_lower for kw in ["退款", "退货", "换货", "售后", "投诉", "工单"]):
        return ["查询物流状态", "联系人工客服"]
    return ["查询订单物流", "申请售后工单", "联系人工客服"]
