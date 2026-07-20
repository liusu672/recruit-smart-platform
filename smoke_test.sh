#!/usr/bin/env bash
# 冒烟测试脚本 —— 启动项目后运行
# 用法: bash smoke_test.sh

BASE="http://localhost:8080"
PASS=0
FAIL=0

check() {
  local name="$1"
  local method="$2"
  local url="$3"
  local data="$4"
  local expect="$5"
  
  if [ "$method" = "GET" ]; then
    code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE$url")
  else
    code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$BASE$url" \
      -H "Content-Type: application/json" -d "$data")
  fi
  
  if [ "$code" = "$expect" ]; then
    echo "  ✅ $name (HTTP $code)"
    PASS=$((PASS + 1))
  else
    echo "  ❌ $name (期望 $expect, 实际 $code)"
    FAIL=$((FAIL + 1))
  fi
}

echo "========================================"
echo "  招聘智能平台 - 冒烟测试"
echo "========================================"
echo ""

# 1. Swagger API文档
echo "--- API文档 ---"
check "Swagger UI"     GET "/swagger-ui/index.html" "" "200"
check "API Docs JSON"  GET "/v3/api-docs" "" "200"

# 2. 认证模块
echo ""
echo "--- 认证 ---"
check "登录成功"   POST "/api/auth/login" \
  '{"username":"admin","password":"123456"}' "200"
check "登录失败"   POST "/api/auth/login" \
  '{"username":"admin","password":"wrong"}' "400"

# 3. 候选人模块
echo ""
echo "--- 候选人 ---"
check "候选人列表" GET "/api/candidates/page" "" "200"

# 4. 职位模块
echo ""
echo "--- 职位 ---"
check "职位列表" GET "/api/job-positions/page" "" "200"

# 5. Dashboard
echo ""
echo "--- 仪表盘 ---"
check "仪表盘概览" GET "/api/dashboard/overview" "" "200"

# 6. 面试模块
echo ""
echo "--- 面试 ---"
check "面试列表" GET "/api/interviews/page" "" "200"

echo ""
echo "========================================"
echo "  结果: $PASS 通过, $FAIL 失败"
echo "========================================"

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
