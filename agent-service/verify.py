# -*- coding: utf-8 -*-
import sys, io, requests, json
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
body = {"user_id": "1", "session_id": "v3", "message": "你好"}
r = requests.post("http://localhost:8000/api/chat", json=body, timeout=30, proxies={"http": None, "https": None})
print(f"Status: {r.status_code}")
data = r.json()
has_hello = any(w in data['reply'] for w in ['你好','您好','欢迎','帮助','客服','什么','问题'])
print(f"Reply OK (has greeting): {has_hello}")
print(f"Suggestions: {json.dumps(data.get('suggestions', []), ensure_ascii=False)}")
if has_hello:
    print("=== AGENT VERIFIED SUCCESS ===")
else:
    print("=== MAY HAVE ISSUE ===")
