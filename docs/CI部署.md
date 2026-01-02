下面是一份**可直接放到仓库 /docs/ci-cd.md** 的“完整文档”。我把“踩坑流水账”压到最少，只保留**问题 → 根因 → 解决方案**，以及你后续最需要的“怎么复现 / 怎么验证 / 遇到同类问题怎么改”。

---

# 苍穹外卖后端 CI/CD 自动部署（GitHub Actions → 服务器 Docker Compose）

## 1. 目标与约束

### 目标

* 只要 `push master`（或手动触发），自动把后端部署到服务器：

    1. 同步最新代码到服务器指定目录
    2. 在服务器上执行 `docker compose build` 构建后端镜像
    3. `docker compose up -d` 更新后端容器

### 约束（本次最终方案的核心设计）

* **服务器不做 git pull**（避免鉴权/密钥/https 用户名密码问题）
* **不在 workflow 里使用 sudo**（GitHub Actions 的 SSH 远程命令是非交互环境，sudo 会要密码直接炸）
* **服务器构建镜像时不主动拉取基础镜像**（`--pull=false`，避免 Docker Hub 网络超时）

---

## 2. 出现过的关键问题（精简版）

### 问题 A：服务器执行 git pull 失败

**现象**
GitHub Actions 通过 SSH 到服务器后执行 `git fetch/reset`，报：

* `fatal: could not read Username for 'https://github.com': No such device or address`

**根因**
服务器仓库 `origin` 是 **HTTPS**：

* `https://github.com/xiangzi-yuan/skyBackend.git`
  非交互环境无法弹出用户名密码输入，所以直接失败。

**解决方案（最终采用）**
**彻底不在服务器上做 git pull**，改成：

* Actions 使用 `actions/checkout` 拉取代码（runner 端）
* 用 `appleboy/scp-action` 把代码目录同步到服务器 `/opt/sky/skyBackend`
* 服务器只负责 `docker compose build/up`

> 这一步直接把所有“服务器 git 鉴权、DeployKey、SSH config、origin 地址”等复杂度干掉。

---

### 问题 B：Docker Hub 拉取基础镜像超时

**现象**

* `dial tcp ... i/o timeout`
  发生在 `FROM maven:...` 或 `FROM eclipse-temurin:...` 等基础镜像元数据拉取阶段。

**根因**
服务器到 Docker Hub 网络不稳定，build 默认可能会尝试拉取/校验远端元数据。

**解决方案（最终采用）**
构建时加：

* `docker compose build --pull=false backend`

并且在服务器提前一次性执行：

* `docker pull maven:3.9.6-eclipse-temurin-17`
* `docker pull eclipse-temurin:17-jre`

这样后续构建基本不再触发远端拉取导致的超时。

---

### 问题 C：workflow 里跑 sudo 失败

**现象**

* `sudo: a terminal is required to read the password`
* `sudo: a password is required`

**根因**
GitHub Actions 远程 SSH 执行命令没有交互式终端，sudo 需要密码输入。

**解决方案（最终采用）**

* workflow 中**不出现 sudo**
* 服务器上把目录权限、docker 组等**提前一次性配置好**（见第 4 节）

---

### 问题 D：为什么会看到“Downloading … / extracting …”很多输出？

这里要分清两类“下载”：

1. **Action 自身容器**在 GitHub Runner 上拉镜像层
   例如 `ghcr.io/appleboy/drone-scp`、`drone-ssh` 的 layer 解压输出
   这不是你的后端，也不是 Maven。

2. **Maven 依赖**在 Docker build 过程中下载
   只要缓存没命中、或依赖确实变化，就会出现 `Downloading from central ...`

最终我们达成的状态是：

* **Java 代码小改动** → Maven 编译层大概率 `CACHED`，几乎不再刷屏
* **pom.xml 改动** → 必然会下载新增/升级的依赖（这是正常且不可避免）

---

## 3. 最终方案架构（你现在跑通的就是这个）

### 数据流

1. GitHub Actions runner：checkout 代码
2. runner → 服务器：SCP 同步目录 `/opt/sky/skyBackend`
3. 服务器本地：`cd /opt/sky && docker compose build --pull=false backend`
4. 服务器本地：`docker compose up -d backend`

### 为什么这样最稳

* 服务器不碰 git：**没有鉴权问题**
* 不用 sudo：**没有非交互密码问题**
* 不 pull 基础镜像：**降低 Docker Hub 抽风概率**
* 构建发生在服务器：镜像直接用于本机 compose，不用额外推镜像仓库

---

## 4. 服务器需要配合做什么（一次性做完，以后不动）

> 你已经做过大部分，这里给“标准最终版”，以后对照检查即可。

### 4.1 用户权限与目录

```bash
# 1) 目录存在
sudo mkdir -p /opt/sky

# 2) 目录归属给部署用户（你是 yuan）
sudo chown -R yuan:yuan /opt/sky

# 3) 确认当前用户在 docker 组（有 docker 就行）
id
# 输出里应包含 docker
# groups 也可以
groups
```

如果没有 docker 组：

```bash
sudo usermod -aG docker yuan
# 重新登录 shell 才生效
```

### 4.2 compose 文件位置固定

确保：

* `/opt/sky/docker-compose.yml` 存在
* workflow 会 `cd /opt/sky && docker compose ...`，所以必须在这个目录

验证：

```bash
cd /opt/sky && docker compose config -q && echo "compose 文件OK"
cd /opt/sky && docker compose ps
```

### 4.3 基础镜像预拉（解决 Docker Hub 超时）

```bash
docker pull maven:3.9.6-eclipse-temurin-17
docker pull eclipse-temurin:17-jre
```

---

## 5. GitHub 侧需要配置什么

### 5.1 Secrets（仓库 Settings → Secrets and variables → Actions）

至少需要：

* `SERVER_HOST`：服务器 IP/域名
* `SERVER_USER`：服务器用户名（如 `yuan`）
* `SSH_PRIVATE_KEY`：用于 SSH 登录服务器的私钥（对应服务器 `authorized_keys`）

> 注意：我们最终方案不要求服务器能访问 GitHub，不需要 DeployKey。

---

## 6. 最终版 workflow（deploy.yml）

你仓库路径：

* `.github/workflows/deploy.yml`

核心点：

* 使用 `scp-action` 同步代码到 `/opt/sky/skyBackend`
* `ssh-action` 执行 `docker compose build --pull=false` + `up -d`
* 不要 sudo

（示例结构如下，你按你现有的就行，关键点别变）

* checkout
* scp upload（排除 `.git`、`target` 等）
* ssh deploy（build --pull=false + up）

---

## 7. 构建与依赖缓存：你最关心的两个问题

### 7.1 “以后 Maven 依赖少了/多了，还能正常下载吗？”

能。只要服务器能访问 Maven Central（`repo.maven.apache.org`）：

* **新增依赖**：会自动下载新增 jar
* **删除依赖**：不会自动删除本地缓存，但不影响构建
* **升级版本**：会下载新版本（旧版本仍可能保留在缓存里）

### 7.2 “为什么我明明改了一点代码，偶尔又看到很多 Downloading？”

可能原因只有三类（按常见程度排序）：

1. 你实际改动触发了 Dockerfile 编译层缓存失效（例如复制顺序/上下文变化）
2. 服务器 BuildKit 缓存被 GC 回收（磁盘紧张、长期没用）
3. 你改了 pom.xml 或父 pom 版本（那下载是必然的）

你可以用这条判断是不是“真的重新下依赖”：

```bash
cd /opt/sky
docker compose build --pull=false backend | grep -E "Downloading from central|Downloaded from central" | head
```

* 如果输出很少：正常
* 如果成千上万：说明缓存没命中或缓存被回收

---

## 8. 运行验证与故障排查（最短路径）

### 8.1 看部署是否成功

```bash
cd /opt/sky
docker compose ps
```

### 8.2 看后端日志

```bash
cd /opt/sky
docker compose logs -n 200 backend
```

### 8.3 健康检查 unhealthy

你目前 `backend` 频繁显示 unhealthy/starting。排查优先级：

1. 先看 healthcheck 具体失败原因：

```bash
docker inspect -f '{{json .State.Health}}' sky-backend | head -c 2000; echo
```

2. 再结合日志看是否 DB/Redis 连接异常、端口不通、配置不对等。

---

## 9. 最终结论（本次交付状态）

* **CI/CD 链路已跑通**：push master → 自动同步代码 → 服务器构建 → 更新容器
* **解决了三类致命不稳定因素**：

    * 服务器 git 鉴权（彻底绕开）
    * sudo 非交互失败（彻底移除）
    * Docker Hub 超时（`--pull=false` + 预拉基础镜像）
* **构建输出已显著收敛**：多数情况下 Maven/编译层命中缓存，只剩少量 action 自身 layer 输出（可忽略）

---
下面给你**两个关键文件的最终版完整内容**：

1. `.github/workflows/deploy.yml`
2. `backend/Dockerfile`

我按你现在已经跑通的方案定稿：**checkout → scp 到 /opt/sky/skyBackend → 服务器 docker compose build --pull=false + up -d**。不再引入 git pull、不用 sudo、不改你服务器结构。

---

## 1) `.github/workflows/deploy.yml`

```yml
# 苍穹外卖后端 - 自动部署到服务器
# push 到 master 分支时自动触发

name: Deploy Backend

on:
  push:
    branches: [master]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - name: 拉取代码
        uses: actions/checkout@v4

      - name: 上传代码到服务器（/opt/sky/skyBackend）
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          # 同步整个仓库到 /opt/sky/skyBackend，但排除无用/大文件目录
          source: ".,!.git,!.git/**,!**/target/**,!.idea,!.idea/**"
          target: "/opt/sky/skyBackend"
          overwrite: true

      - name: 服务器构建并启动（不拉取基础镜像）
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          command_timeout: 100m
          script: |
            set -euo pipefail
            cd /opt/sky

            export DOCKER_BUILDKIT=1
            export COMPOSE_DOCKER_CLI_BUILD=1

            # 关键点：不 pull 远端基础镜像，避免 Docker Hub/registry 抽风超时
            docker compose build --pull=false backend

            # 启动/更新
            docker compose up -d backend

            docker compose ps
            echo "✅ 后端部署完成"
```

**注意：** 这份 workflow 假设你服务器上 `/opt/sky/docker-compose.yml` 已经存在并可用（你已经满足）。
并且部署用户（yuan）已经在 docker 组里且 `/opt/sky` 权限 OK（你也已经做了）。

---

## 2) `backend/Dockerfile`

现在 Dockerfile 里已经是多阶段构建，但你后面日志里显示你实际在用的是 `mvn -B -DskipTests clean package`（没有 go-offline 离线模式）。
这份最终版我给你一个**稳定、最少变动、缓存更可靠**的版本：

* 先只 COPY 各模块 pom.xml（提升缓存命中）
* 再 COPY 源码
* 用 `--mount=type=cache,target=/root/.m2` 保留 Maven 缓存（BuildKit）
* **不强制离线**（-o），因为离线会导致“第一次缺依赖直接失败”，对你这种服务器网络环境不稳反而更烦

```dockerfile
# syntax=docker/dockerfile:1.4

# ============================================
# 苍穹外卖后端 - Docker 多阶段构建（最终版）
# ============================================

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# 可选：如果你仓库里确实有 mvn-settings.xml（用于国内镜像/加速），保留
# 没有就把这两行删除（否则会 build 失败）
COPY mvn-settings.xml /usr/share/maven/conf/settings.xml

# 1) 只拷贝 POM（让“依赖层”尽量缓存命中）
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 2) 拷贝源码（源码变动只影响后面的编译层）
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 3) 编译打包（使用 BuildKit cache 持久化 /root/.m2）
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -pl sky-server -am \
    clean package

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre
WORKDIR /app

# 创建低权限用户
RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app

# 拷贝 jar
COPY --from=builder /build/sky-server/target/sky-server-*.jar /app/app.jar

USER appuser
EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
```

---

* **改 pom.xml**：下载依赖是正常的
* **只改 Java 代码**：理论上大多数时候会命中缓存，输出会少
