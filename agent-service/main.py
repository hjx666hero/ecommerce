"""
FastAPI 应用入口
提供智能客服 Agent 的 RESTful API 接口。
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv
import os

# ============================================================
# 加载 .env 文件中的环境变量（必须在导入 agent_service 之前）
# 使用绝对路径确保在任何工作目录下都能找到
# ============================================================
env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
load_dotenv(env_path)

import agent_service
import memory

# ============================================================
# FastAPI 应用初始化
# ============================================================
app = FastAPI(title="拼夕夕智能客服Agent")

# ============================================================
# CORS 跨域中间件配置：允许所有来源的请求
# ============================================================
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============================================================
# 请求/响应数据模型定义
# ============================================================
class ChatRequest(BaseModel):
    """聊天请求体"""
    user_id: str       # 用户唯一标识符
    session_id: str    # 会话唯一标识符
    message: str       # 用户消息文本


class ChatResponse(BaseModel):
    """聊天响应体"""
    reply: str                       # AI 回复内容
    suggestions: list[str] = []      # 后续建议选项


# ============================================================
# API 路由定义
# ============================================================
@app.post("/api/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest):
    """智能客服聊天接口。

    接收用户消息，调用 LangChain Agent 进行推理处理，
    返回 AI 回复和后续建议。

    Args:
        request: 包含 user_id、session_id、message 的请求体

    Returns:
        ChatResponse: 包含 AI 回复和建议列表的响应体
    """
    # 校验必填字段
    if not request.user_id or not request.session_id or not request.message:
        raise HTTPException(status_code=400, detail="user_id、session_id 和 message 不能为空")

    # 调用 Agent 服务处理消息
    result = agent_service.process_message(
        request.user_id,
        request.session_id,
        request.message,
    )

    return ChatResponse(
        reply=result["reply"],
        suggestions=result.get("suggestions", []),
    )


@app.get("/health")
async def health_check():
    """健康检查接口。

    用于服务监控和负载均衡器检测服务是否正常运行。

    Returns:
        包含服务状态的 JSON 对象
    """
    return {"status": "ok"}


@app.delete("/session/{session_id}")
async def clear_session(session_id: str):
    """清除指定会话的对话历史。

    Args:
        session_id: 要清除的会话唯一标识符

    Returns:
        包含清除结果的 JSON 对象
    """
    memory.clear_session(session_id)
    return {"status": "ok", "session_id": session_id}


# ============================================================
# 应用启动入口
# ============================================================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
