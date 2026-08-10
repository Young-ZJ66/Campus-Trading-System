# 校园闲置物品交易系统 (Campus Trading System)

基于 **Spring Boot 3 + Vue 3** 开发的高品质校园二手物品及闲置物品交易平台，旨在通过更规范安全的交易流程和趣味性的积分商城促进校园绿色循环。

---

## 核心特性

- **双交易通道**：支持直接使用系统模拟金额"普通购买"与以闲置交换的"以物换物"两种模式。
- **并发安全锁定**：系统引入了"置换商品锁定机制"与乐观锁（CAS），防止一物多售；在同意、拒绝、买卖家取消或系统级联判定冲突时，状态机将自动释放商品状态。
- **积分循环体系**：整合每日签到、交易完成加分等积分激励路径，用户可在"积分商城"换购专属的校园好礼。
- **Sa-Token 权限鉴权**：全面接入 Sa-Token 鉴权框架，实现优雅、无感的用户登录状态维护与动态管理员角色（Admin）鉴权拦截。
- **BCrypt 密码加密**：用户密码使用 BCrypt + 随机盐值加密存储，安全性远超 MD5。
- **统一设计令牌**：前端基于 CSS 变量的设计令牌体系，统一管理颜色、字体、阴影、圆角、过渡等视觉变量。
- **独立后台系统**：内置完整的管理员端。包含可视化仪表盘、分类管理、资讯发布、全局用户管理和订单仲裁。

---

## 技术选型

### 后端 (Backend)

- **核心框架**：Spring Boot 3.x
- **持久层框架**：MyBatis / MyBatis-Spring
- **安全鉴权**：Sa-Token 1.37.0+
- **密码加密**：BCrypt（Sa-Token 内置）
- **参数校验**：JSR-303 Bean Validation（spring-boot-starter-validation）
- **API 文档**：Knife4j 4.5（OpenAPI 3）
- **数据库**：MySQL 8.0+
- **连接池**：HikariCP

### 前端 (Frontend)

- **核心框架**：Vue 3 (Composition API)
- **构建工具**：Vite 8.x
- **状态管理**：Pinia
- **路由控制**：Vue Router 5.x
- **UI 组件库**：Element Plus
- **图标系统**：@element-plus/icons-vue
- **图表库**：ECharts 6
- **网络请求**：Axios
- **设计令牌**：CSS Custom Properties（全局变量体系）

---

## 极速部署指南

### 1. 准备工作

请确保本地已安装：

- **JDK 17** 及以上
- **MySQL 8.0** 及以上
- **Node.js 20.19+ / 22.12+** (建议使用 npm)

### 2. 后端部署 (`backend`)

1. 打开 MySQL，创建名为 `campus_trading` 的数据库：
   ```sql
   CREATE DATABASE campus_trading CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. 执行项目中的 SQL 初始化脚本：导入 `backend/src/main/resources/sql/schema.sql` 文件以创建表结构并加载测试数据。
3. 配置数据库连接（修改 `application.yml` 中的用户名与密码）：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/campus_trading?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
       username: 您的MySQL用户名
       password: 您的MySQL密码
   ```
4. 运行 `com.campus.CampusTradingApplication` 主类即可启动后端，默认服务端口为 `8080`。
    注意：图片上传目录基于 `${user.dir}/uploads/`，请确保在 `backend` 目录下启动后端，否则图片上传与访问路径会错位。
5. API 文档地址：启动后访问 `http://localhost:8080/doc.html`

### 3. 前端部署 (`front`)

1. 进入 `front` 目录：
   ```bash
   cd front
   ```
2. 安装依赖包：
   ```bash
   npm install
   ```
3. 配置 API 地址（可选，默认已提供开发环境配置）：
   ```bash
   # 编辑 .env 文件
   VITE_API_BASE_URL=http://localhost:8080
   ```
4. 运行本地开发服务器：
   ```bash
   npm run dev
   ```
5. 启动成功后，在浏览器访问控制台打印的地址（如 `http://localhost:5173`）即可。

---

## 测试账号

数据库初始化后，以下账号可用（密码均为 `123456`）：

| 学号 | 昵称 | 角色 |
|------|------|------|
| admin | 系统管理员 | 管理员 |
| 20220001 | 张三 | 普通用户 |
| 20220002 | 李四 | 普通用户 |
| 20220003 | 王五 | 普通用户 |
| 20220004 | 赵六 | 普通用户 |
| 20220005 | 陈七 | 普通用户 |
| 20220006 | 周八 | 普通用户 |
| 20220007 | 吴九 | 普通用户 |

---

## 项目结构一览

```text
├── backend                         # 后端 Spring Boot 工程
│   ├── src/main/java/com/campus/
│   │   ├── common/                 # 通用工具（Result 封装、登录限流等）
│   │   ├── config/                 # 配置类（WebConfig、拦截器、Sa-Token）
│   │   ├── controller/             # 控制器（用户端 + 管理端）
│   │   ├── exception/              # 全局异常处理
│   │   ├── mapper/                 # MyBatis Mapper 接口
│   │   ├── pojo/                   # 实体类、DTO、枚举
│   │   └── service/impl/          # 业务逻辑实现
│   ├── src/main/resources/
│   │   ├── mapper/                 # MyBatis XML 映射文件
│   │   ├── sql/schema.sql          # 数据库建表与初始化脚本
│   │   └── application.yml         # 应用配置（支持环境变量）
│   └── pom.xml                     # Maven 依赖配置
├── front                           # 前端 Vue 3 工程
│   ├── public/                     # 静态资源及 favicon
│   ├── src/
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 全局复用组件
│   │   ├── views/                  # 核心页面（含 admin/ 子目录）
│   │   ├── router/                 # 路由配置
│   │   ├── store/                  # Pinia 状态管理
│   │   ├── utils/                  # 工具函数（request、image、time）
│   │   └── style.css               # 全局设计令牌
│   ├── .env                        # 开发环境变量
│   ├── .env.production             # 生产环境变量
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## 联系方式

如有问题或需要商业服务，欢迎联系：

- **QQ**: `1600386893`
- **服务内容**: 付费部署、定制修改、功能扩展等

## License

MIT License
