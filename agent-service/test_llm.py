"""快速测试LLM连通性"""
import os, sys, requests
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
os.chdir(os.path.dirname(os.path.abspath(__file__)))
from dotenv import load_dotenv
load_dotenv(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env"))

url = os.getenv("OPENAI_BASE_URL", "") + "/chat/completions"
key = os.getenv("OPENAI_API_KEY", "")
model = os.getenv("MODEL_NAME", "qwen3.7-plus")

print(f"URL: {url}")
print(f"Model: {model}")
print(f"Key starts with: {key[:15]}...")

resp = requests.post(
    url,
    json={"model": model, "messages": [{"role": "user", "content": "说你好"}]},
    headers={"Authorization": f"Bearer {key}", "Content-Type": "application/json"},
    timeout=30,
    proxies={"http": None, "https": None}
)
print(f"Status: {resp.status_code}")
data = resp.json()
print(f"Reply: {data['choices'][0]['message']['content']}")
print("OK - LLM 连通正常")
