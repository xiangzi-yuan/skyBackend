# Sky Takeout Backend (苍穹外卖后端)

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.3-green)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Redis](https://img.shields.io/badge/Redis-latest-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

一个基于 Spring Boot 的外卖点餐系统后端服务，提供完整的 B 端（商家管理后台）和 C 端（微信小程序用户端）API 接口。

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [功能模块](#功能模块)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 文档](#api-文档)
- [部署指南](#部署指南)

## 📖 项目简介

Sky Takeout（苍穹外卖）是一个完整的外卖订餐系统后端解决方案，支持：

- **商家管理端 (Admin)**：员工管理、分类管理、菜品管理、套餐管理、订单处理、数据统计等
- **用户端 (User)**：微信登录、菜品浏览、购物车、下单、地址管理等
- **消息推送**：基于 WebSocket 的订单实时通知

## 🛠 技术栈

| 类别 | 技术 |
|------|------|
| **核心框架** | Spring Boot 2.7.3, Spring MVC |
| **持久层** | MyBatis 3.5.19, Druid 连接池 |
| **数据库** | MySQL 8.x, Redis |
| **安全认证** | JWT (jjwt 0.9.1) |
| **API 文档** | Knife4j 3.0.2 (Swagger 增强) |
| **对象映射** | MapStruct 1.6.3, Lombok 1.18.36 |
| **文件存储** | 阿里云 OSS |
| **报表导出** | Apache POI 3.16 |
| **支付集成** | 微信支付 API v3 |
| **实时通信** | WebSocket |
| **构建工具** | Maven 3.x |
| **容器化** | Docker |

## 📁 项目结构

```
skyBackend/
├── sky-common/          # 公共模块 - 工具类、常量、异常处理等
├── sky-pojo/            # 数据模型模块
│   ├── dto/             # 数据传输对象 (Data Transfer Objects)
│   ├── entity/          # 数据库实体类
│   ├── vo/              # 视图对象 (View Objects)
│   └── readmodel/       # 只读模型
├── sky-server/          # 主服务模块
│   ├── annotation/      # 自定义注解
│   ├── aspect/          # AOP 切面
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   │   ├── admin/       # 管理端 API
│   │   ├── user/        # 用户端 API
│   │   └── notify/      # 通知回调 API
│   ├── converter/       # MapStruct 转换器
│   ├── handler/         # 全局异常处理器
│   ├── interceptor/     # 拦截器 (JWT 认证)
│   ├── mapper/          # MyBatis Mapper 接口
│   ├── service/         # 业务逻辑层
│   ├── task/            # 定时任务
│   ├── util/            # 工具类
│   └── websocket/       # WebSocket 处理
├── docs/                # 项目文档
├── nginx/               # Nginx 配置
└── Dockerfile           # Docker 构建文件
```

## 🎯 功能模块

### 管理端 (Admin)

| 模块 | 接口 | 说明 |
|------|------|------|
| **员工管理** | `/admin/employee` | 员工的增删改查、登录、状态管理 |
| **分类管理** | `/admin/category` | 菜品/套餐分类管理 |
| **菜品管理** | `/admin/dish` | 菜品的增删改查、上下架、口味管理 |
| **套餐管理** | `/admin/setmeal` | 套餐的增删改查、上下架 |
| **订单管理** | `/admin/order` | 订单查询、接单、配送、完成、取消 |
| **数据统计** | `/admin/report` | 营业额、用户、订单统计、报表导出 |
| **工作台** | `/admin/workspace` | 今日运营数据概览 |
| **店铺设置** | `/admin/shop` | 店铺营业状态管理 |
| **文件上传** | `/admin/common` | 图片上传至阿里云 OSS |

### 用户端 (User)

| 模块 | 接口 | 说明 |
|------|------|------|
| **用户登录** | `/user/user` | 微信小程序登录 |
| **分类查询** | `/user/category` | 获取菜品/套餐分类 |
| **菜品查询** | `/user/dish` | 按分类查询菜品列表 |
| **套餐查询** | `/user/setmeal` | 套餐查询及详情 |
| **购物车** | `/user/shoppingCart` | 添加、查看、删除购物车 |
| **地址管理** | `/user/addressBook` | 收货地址的增删改查 |
| **订单** | `/user/order` | 下单、查询、支付、取消、催单 |
| **店铺状态** | `/user/shop` | 获取店铺营业状态 |

## 🚀 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Redis** 6.0+

### 本地运行

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd skyBackend
   ```

2. **配置数据库**
   
   创建数据库并导入初始数据（SQL 脚本位于项目根目录或 `docs` 中）

3. **修改配置**
   
   编辑 `sky-server/src/main/resources/application-dev.yml`，配置以下信息：
   ```yaml
   sky:
     datasource:
       host: localhost
       port: 3306
       database: sky_takeout
       username: your_username
       password: your_password
     redis:
       host: localhost
       port: 6379
       database: 0
     alioss:
       endpoint: oss-cn-xxx.aliyuncs.com
       access-key-id: your-access-key
       access-key-secret: your-access-key-secret
       bucket-name: your-bucket-name
     wechat:
       appid: your-appid
       secret: your-secret
   ```

4. **编译运行**
   ```bash
   mvn clean compile
   mvn -pl sky-server spring-boot:run
   ```

5. **访问 API 文档**
   
   启动后访问：`http://localhost:8080/doc.html`

## ⚙️ 配置说明

项目支持多环境配置:

| 文件 | 说明 |
|------|------|
| `application.yml` | 主配置文件，通用配置 |
| `application-dev.yml` | 开发环境配置 |
| `application-dev-local.yml` | 本地开发配置 (git ignored) |
| `application-prod.yml` | 生产环境配置 |

通过 `spring.profiles.active` 切换环境:
```bash
# 开发环境
java -jar sky-server.jar --spring.profiles.active=dev

# 生产环境
java -jar sky-server.jar --spring.profiles.active=prod
```

## 📚 API 文档

项目集成了 **Knife4j** (Swagger 增强版)，提供交互式 API 文档。

- **文档地址**: `http://localhost:8080/doc.html`
- **文档分组**:
  - 管理端接口
  - 用户端接口

## 🐳 部署指南

### Docker 部署

1. **构建镜像**
   ```bash
   docker build -t sky-takeout-backend .
   ```

2. **运行容器**
   ```bash
   docker run -d \
     --name sky-backend \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e SKY_DATASOURCE_HOST=mysql-host \
     -e SKY_REDIS_HOST=redis-host \
     sky-takeout-backend
   ```

### 环境变量

| 变量 | 说明 | 示例 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `prod` |
| `SKY_DATASOURCE_HOST` | MySQL 主机地址 | `127.0.0.1` |
| `SKY_DATASOURCE_PORT` | MySQL 端口 | `3306` |
| `SKY_REDIS_HOST` | Redis 主机地址 | `127.0.0.1` |
| `SKY_REDIS_PORT` | Redis 端口 | `6379` |
| `JAVA_OPTS` | JVM 参数 | `-Xmx512m` |

## 📂 相关文档

更多开发文档请参阅 `docs/` 目录:

- [开发规范](docs/DEVELOPMENT_SPECIFICATION.md)
- [重构总结](docs/REFACTORING_SUMMARY.md)
- [构建缓存指南](docs/sky-backend-build-cache-guide.md)
- [运维文档](docs/运维.md)

## 📄 License

本项目仅供学习参考使用。
