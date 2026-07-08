# 校园闲置物品交易系统 (Campus Trading System)

基于 **Spring Boot 3 + Vue 3** 开发的高品质校园二手物品及闲置物品交易平台，旨在通过更规范安全的交易流程和趣味性的积分商城促进校园绿色循环。

---

## 🌟 核心特性

- 🛍️ **双交易通道**：支持直接使用系统模拟金额“普通购买”与以闲置交换的“以物换物”两种模式。
- 🔒 **并发安全锁定**：系统引入了“置换商品锁定机制”。当买家发起换物申请时，其交换物品将进入锁定状态（`3`），防止一物多售；在同意、拒绝、买卖家取消或系统级联判定冲突时，状态机将自动释放商品状态。
- 🪙 **积分循环体系**：整合每日签到、交易完成加分等积分激励路径，用户可在“积分商城”换购专属的校园好礼。
- 🛡️ **Sa-Token 权限鉴权**：全面接入 Sa-Token 鉴权框架，实现优雅、无感的用户登录状态维护与动态管理员角色（Admin）鉴权拦截。
- 🎨 **企业级 UI/UX**：界面完全采用 Element Plus 精美矢量图标与平滑微动画重构，剔除不合时宜的系统 Emoji 符号，并重新设计了与主视觉契合的简约 Favicon 浏览器图标。
- 📊 **独立后台系统**：内置完整的管理员端。包含可视化仪表盘、分类管理、资讯发布、全局用户管理和订单仲裁。

---

## 🛠️ 技术选型

### 后端 (Backend)
- **核心框架**：Spring Boot 3.x
- **持久层框架**：MyBatis / MyBatis-Spring
- **安全鉴权**：Sa-Token 1.37.0+
- **数据库**：MySQL 8.0+
- **连接池**：Druid / HikariCP

### 前端 (Frontend)
- **核心框架**：Vue 3 (Composition API)
- **构建工具**：Vite 5.x
- **状态管理**：Pinia
- **路由控制**：Vue Router 4.x
- **UI 组件库**：Element Plus
- **网络请求**：Axios
- **图标系统**：@element-plus/icons-vue + SVG

---

## 🚀 极速部署指南

### 1. 准备工作
请确保本地已安装：
- **JDK 17** 及以上
- **MySQL 8.0** 及以上
- **Node.js 18** 及以上 (建议使用 npm 或 pnpm)

---

### 2. 后端部署 (`backend`)
1. 打开 MySQL，创建名为 `campus_trading` 的数据库：
   ```sql
   CREATE DATABASE campus_trading CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. 执行项目中的 SQL 初始化脚本：导入 [schema.sql](file:///E:/Project/基于Spring Boot的校园闲置物交易系统/backend/src/main/resources/sql/schema.sql) 文件以创建表结构并加载测试数据。
3. 修改 [application.yml](file:///E:/Project/基于Spring Boot的校园闲置物交易系统/backend/src/main/resources/application.yml) 配置文件中的数据库用户名与密码：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/campus_trading?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
       username: 您的MySQL用户名
       password: 您的MySQL密码
   ```
4. 运行 `com.campus.CampusApplication` 主类即可启动后端，默认服务端口为 `8080`。

---

### 3. 前端部署 (`front`)
1. 进入 `front` 目录：
   ```bash
   cd front
   ```
2. 安装依赖包：
   ```bash
   npm install
   ```
3. 运行本地开发服务器：
   ```bash
   npm run dev
   ```
4. 启动成功后，在浏览器访问控制台打印的地址（如 `http://localhost:5173`）即可。

---

## 📂 项目结构一览

```text
├── backend                 # 后端 Spring Boot 工程
│   ├── src/main/java       # Java 源码目录
│   └── src/main/resources  # 配置文件、Mapper.xml 与 SQL 脚本
├── front                   # 前端 Vue 3 工程
│   ├── public/             # 静态资源及 favicon.svg
│   ├── src/                # 前端业务核心源码
│   │   ├── components/     # 全局复用组件（如登录、注册、导航栏）
│   │   ├── views/          # 核心页面（商城、个人中心、资讯及后台）
│   │   ├── router/         # 路由配置与守卫拦截
│   │   └── store/          # Pinia 状态管理
│   ├── package.json        # 前端依赖配置
│   └── vite.config.js      # Vite 构建配置文件
└── README.md
```
