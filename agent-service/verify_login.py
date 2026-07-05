# -*- coding: utf-8 -*-
import sys, io, requests, json
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# 验证1: 不带 token 访问需要登录的接口，确认返回 200 + "请先登录"
r = requests.get("http://localhost:8081/api/coupon/my", timeout=10, proxies={"http": None, "https": None})
data = r.json()
print(f"验证1 - 未登录访问 /api/coupon/my:")
print(f"  HTTP Status: {r.status_code}")
print(f"  code: {data.get('code')}")
print(f"  message: {data.get('message')}")
assert r.status_code == 200, f"FAIL: 期望200, 实际{r.status_code}"
assert data['code'] == 401, f"FAIL: 期望code=401"
assert data['message'] == '请先登录', f"FAIL: 期望'请先登录'"
print("  PASSED")

# 验证2: 带无效 token 访问，同理返回 200 + "请先登录"
h = {"Authorization": "Bearer invalid_token_xxx"}
r = requests.get("http://localhost:8081/api/coupon/available", headers=h, timeout=10, proxies={"http": None, "https": None})
data = r.json()
print(f"\n验证2 - 无效token访问 /api/coupon/available:")
print(f"  HTTP Status: {r.status_code}")
print(f"  code: {data.get('code')}")
print(f"  message: {data.get('message')}")
assert r.status_code == 200, f"FAIL: 期望200, 实际{r.status_code}"
assert data['code'] == 401, f"FAIL: 期望code=401"
assert data['message'] == '请先登录', f"FAIL: 期望'请先登录'"
print("  PASSED")

print("\n=== 全部验证通过 ===")
