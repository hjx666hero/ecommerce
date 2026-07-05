# 拼夕夕 - 全栈电商平台

基于 Spring Boot 3 + Vue 3 的前后端分离电商平台，涵盖完整的电商业务链路，包括商品浏览、购物车、订单、秒杀、支付、优惠券、评价、智能客服等功能。

## 项目截图

（请替换为实际截图链接）

## 技术架构

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 应用框架 |
| Java | 17 | 开发语言 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 6.x+ | 缓存 / 分布式锁 / 秒杀库存 |
| Spring Security | 6.x | 安全认证 |
| JWT (jjwt) | 0.12.3 | 无状态鉴权（双Token机制） |
| Hutool | 5.8.25 | 工具库（雪花算法等） |
| Maven | 3.x | 项目构建管理 |

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4.21 | 前端框架（Composition API） |
| Vite | 5.2.0 | 构建工具 |
| Vue Router | 4.3.0 | 前端路由 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.6.3 | UI 组件库 |
| Axios | 1.6.8 | HTTP 请求 |

### 智能客服

| 组件 | 技术 | 说明 |
|------|------|------|
| Agent Service | Python / FastAPI | LLM 驱动的智能客服后端 |

---

## 项目结构

```
ecommerce/
├── ecommerce-common/          # 公共模块（配置、工具类、拦截器、异常处理）
│   └── src/main/java/com/ecommerce/common/
│       ├── config/            # Jackson、MyBatis-Plus、Redis、Security、Web 配置
│       ├── constant/          # 订单状态、Redis Key 常量
│       ├── exception/         # 全局异常处理
│       ├── interceptor/       # JWT 登录拦截器
│       ├── result/            # 统一响应体 Result、错误码枚举
│       ├── util/              # JwtUtil、RedisUtil（含分布式锁 + Lua 脚本）
│       └── annotation/        # @RequireLogin 自定义注解
│
├── ecommerce-mall/            # 商城核心业务模块（端口 8081）
│   └── src/main/java/com/ecommerce/mall/
│       ├── controller/        # 14 个 API 控制器
│       ├── service/           # 12 个业务服务接口与实现
│       ├── entity/            # 21 个数据库实体
│       ├── mapper/            # MyBatis Mapper 接口
│       ├── dto/               # 数据传输对象
│       ├── vo/                # 视图对象
│       └── task/              # 定时任务（订单超时、优惠券过期、消息补偿、秒杀预热）
│
├── ecommerce-admin/           # 管理后台模块（端口 8082）
│   └── src/main/java/com/ecommerce/admin/
│       ├── controller/        # 9 个管理 API 控制器
│       ├── service/           # 8 个管理服务类
│       ├── entity/            # AdminUser 实体
│       └── util/              # 密码生成工具
│
├── ecommerce-generator/       # MyBatis-Plus 代码生成器（Velocity 模板）
│
├── ecommerce-vue-mall/        # Vue 3 前端项目（端口 5173）
│   └── src/
│       ├── views/             # 28 个页面视图
│       ├── components/        # 5 个公共组件
│       ├── api/               # 12 个 API 请求模块
│       ├── stores/            # Pinia 状态管理
│       └── router/            # 路由配置 + 路由守卫
│
├── agent-service/             # Python 智能客服服务
│   ├── main.py                # FastAPI 入口
│   └── verify_login.py        # 登录验证
│
└── pom.xml                    # Maven 父 POM
```

---

## 功能模块

### 商城前端

| 模块 | 功能说明 |
|------|----------|
| 首页 | 轮播图、热门商品、新品推荐、分类导航、公告 |
| 商品 | 分类浏览、关键词搜索、多条件筛选排序、商品详情（含 SKU 规格选择）、商品收藏 |
| 购物车 | 添加/删除/修改数量、全选/单选、批量删除、清空购物车 |
| 订单 | 创建订单、订单列表（按状态筛选）、订单详情、取消订单、确认收货 |
| 支付 | 模拟支付、退款处理 |
| 秒杀 | 秒杀活动列表（倒计时）、秒杀详情、限时抢购（Redis 库存预热 + Lua 原子扣减） |
| 优惠券 | 领券中心、我的优惠券、下单时自动计算优惠 |
| 评价 | 商品评价（1-5 星评分）、商家回复 |
| 用户 | 注册/登录（密码 + 短信验证码）、个人信息、修改密码、地址管理 |
| 消息 | 消息中心、未读消息提醒 |
| 智能客服 | 悬浮聊天组件，AI 驱动的客服问答 |

### 管理后台

| 模块 | 功能说明 |
|------|----------|
| 仪表盘 | 关键数据统计概览 |
| 商品管理 | SPU/SKU 增删改查、分类管理、品牌管理、上下架 |
| 订单管理 | 订单列表、发货、退款处理、订单统计 |
| 用户管理 | 用户列表、启用/禁用 |
| 优惠券管理 | 优惠券模板 CRUD、批量发放 |
| 秒杀管理 | 秒杀活动 CRUD |
| 轮播图/公告 | 首页轮播图与公告管理 |

---

## 核心亮点

### 1. 秒杀系统
- **接口级限流**：基于 Redis 计数器，每用户每秒最多 50 次请求
- **库存预热**：应用启动时通过 `ApplicationRunner` 将秒杀库存加载到 Redis
- **Lua 原子操作**：Redis 端单次完成库存扣减 + 用户限购检查，无并发竞态
- **MySQL 二次确认**：CAS 原子 SQL 扣减库存，失败则补偿 Redis 库存
- 下单路径极短，延迟可控

### 2. 订单系统
- **分布式锁**：基于 Redis 的 `SET NX EX` 防止用户重复提交
- **雪花算法**：Hutool 生成全局唯一订单号
- **优惠券自动计算**：支持满减券、折扣券、无门槛券
- 订单超时自动取消（定时任务扫描 + 库存回滚 + 优惠券返还）

### 3. 缓存策略
- 首页数据 Redis 缓存 10 分钟，分类树缓存 1 小时
- 缓存故障自动降级，回退数据库查询
- 秒杀详情缓存 1 分钟，保证数据时效性

### 4. 安全与鉴权
- JWT 双 Token 机制：Access Token（2h）+ Refresh Token（7d）
- BCrypt 密码加密，CSRF 关闭，无状态 Session
- 前端路由守卫 + 后端拦截器双重校验

### 5. 分布式可靠性
- 本地消息表实现最终一致性（订单库存回滚消息补偿重试）
- 定时任务兜底（订单超时取消、优惠券过期标记）

### 6. 智能客服
- 基于 LLM 的 Python Agent 服务
- 可查询订单详情、创建售后工单
- 服务不可用时自动降级兜底回复

---

## 数据库设计

共 21 张表，覆盖完整电商业务：

| 表名 | 说明 | 表名 | 说明 |
|------|------|------|------|
| user | 用户表 | order_item | 订单商品明细 |
| address | 收货地址 | pay_record | 支付记录 |
| category | 商品分类（树形） | coupon | 优惠券模板 |
| brand | 品牌 | user_coupon | 用户优惠券 |
| spu | 商品主信息 | seckill_activity | 秒杀活动 |
| sku | 库存单元 | message | 消息通知 |
| cart | 购物车 | carousel | 轮播图 |
| order | 订单主表 | announcement | 公告 |
| admin_user | 管理员 | user_favorite | 用户收藏 |
| product_review | 商品评价 | local_message | 本地消息表 |
| ticket | 售后工单 | | |

---

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+
- Python 3.10+（智能客服可选）

### 1. 数据库初始化

```sql
CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
```

执行项目中的 SQL 初始化脚本创建表结构。

### 2. 启动后端

```bash
# 编译公共模块
cd ecommerce
mvn clean install -pl ecommerce-common -DskipTests

# 启动商城模块（端口 8081）
cd ecommerce-mall
mvn spring-boot:run

# 启动管理后台模块（端口 8082）
cd ecommerce-admin
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd ecommerce-vue-mall
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，已配置代理转发到后端。

### 4. 启动智能客服（可选）

```bash
cd agent-service
pip install -r requirements.txt
python main.py
```

### 管理后台默认账号

- 用户名：`admin`
- 密码：`admin123`

---

## API 接口概览

### 商城 API（端口 8081）

| 路径前缀 | 说明 | 接口数 |
|----------|------|--------|
| `/api/auth/**` | 认证（登录/注册/Token刷新） | 5 |
| `/api/home/**` | 首页数据 | 1 |
| `/api/product/**` | 商品浏览/搜索/收藏 | 8 |
| `/api/cart/**` | 购物车 | 9 |
| `/api/order/**` | 订单 | 7 |
| `/api/pay/**` | 支付 | 3 |
| `/api/coupon/**` | 优惠券 | 4 |
| `/api/seckill/**` | 秒杀 | 3 |
| `/api/review/**` | 评价 | 2 |
| `/api/user/**` | 用户信息 | 3 |
| `/api/address/**` | 收货地址 | 6 |
| `/api/message/**` | 消息通知 | 4 |
| `/api/v1/customer-service/**` | 智能客服 | 1 |
| `/api/internal/**` | 内部调用（Agent） | 2 |

### 管理后台 API（端口 8082）

| 路径前缀 | 说明 | 接口数 |
|----------|------|--------|
| `/admin/auth/**` | 管理员登录 | 1 |
| `/admin/dashboard/**` | 仪表盘 | 1 |
| `/admin/product/**` | 商品/分类/品牌管理 | 10+ |
| `/admin/order/**` | 订单管理 | 5 |
| `/admin/user/**` | 用户管理 | 3 |
| `/admin/coupon/**` | 优惠券管理 | 4 |
| `/admin/seckill/**` | 秒杀管理 | 3 |
| `/admin/carousel/**` | 轮播图管理 | 3 |
| `/admin/announcement/**` | 公告管理 | 3 |

---

## 前端路由一览

| 路由 | 页面 | 需登录 |
|------|------|--------|
| `/` | 首页 | 否 |
| `/login` | 登录 | 否 |
| `/register` | 注册 | 否 |
| `/forget-password` | 忘记密码 | 否 |
| `/category/:id?` | 商品分类 | 否 |
| `/products` | 商品列表 | 否 |
| `/product/:id` | 商品详情 | 否 |
| `/search` | 搜索 | 否 |
| `/cart` | 购物车 | 是 |
| `/order/confirm` | 确认订单 | 是 |
| `/pay/:orderNo` | 支付 | 是 |
| `/orders` | 我的订单 | 是 |
| `/order/:orderNo` | 订单详情 | 是 |
| `/user` | 个人中心 | 是 |
| `/user/info` | 个人信息 | 是 |
| `/user/address` | 地址管理 | 是 |
| `/user/security` | 账号安全 | 是 |
| `/user/coupons` | 我的优惠券 | 是 |
| `/coupons` | 领券中心 | 否 |
| `/favorites` | 我的收藏 | 是 |
| `/seckill` | 秒杀列表 | 否 |
| `/seckill/:id` | 秒杀详情 | 是 |
| `/messages` | 消息中心 | 是 |
| `/admin/coupons` | 管理-优惠券 | 否 |
| `/admin/seckill` | 管理-秒杀 | 否 |

---

## 配置说明

核心配置文件位于：
- `ecommerce-mall/src/main/resources/application.yml`
- `ecommerce-admin/src/main/resources/application.yml`

主要配置项：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce  # 数据库连接
  data:
    redis:
      host: localhost  # Redis 地址
      port: 6379

jwt:
  secret: your-secret-key  # JWT 密钥
  access-token-expire: 7200       # Access Token 有效期（秒）
  refresh-token-expire: 604800    # Refresh Token 有效期（秒）

agent:
  service-url: http://localhost:8000  # 智能客服服务地址
```

---

## 项目复盘总结

### 技术收获

1. **Spring Boot 3 + Java 17**：掌握了新版 Spring 生态的变化，如 `jakarta.*` 命名空间迁移
2. **MyBatis-Plus**：熟练使用代码生成器、逻辑删除、分页插件、条件构造器
3. **Redis 深度应用**：分布式锁、Lua 脚本原子操作、库存预热、缓存降级
4. **秒杀系统设计**：从接口限流、库存预热、原子扣减到补偿回滚的完整方案
5. **Vue 3 Composition API**：全面使用 `<script setup>` 语法，Pinia 状态管理
6. **多模块 Maven 架构**：common / mall / admin / generator 职责分离
7. **分布式可靠性**：本地消息表 + 定时任务补偿的最终一致性方案

### 待优化项

- [ ] 接入真实支付通道（支付宝 / 微信）
- [ ] 接入真实短信服务（阿里云 / 腾讯云）
- [ ] 引入消息队列（RocketMQ / RabbitMQ）替代本地消息表
- [ ] 管理后台前端页面完善（当前仅优惠券和秒杀管理有前端页面）
- [ ] 添加单元测试和集成测试
- [ ] Docker 容器化部署
- [ ] CI/CD 流水线
- [ ] 接口文档（Swagger / Knife4j）
- [ ] 日志系统完善（ELK）
- [ ] 监控告警（Prometheus + Grafana）

---

## License

MIT License
