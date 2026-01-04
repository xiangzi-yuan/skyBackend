# Sky Backend 部署与构建缓存稳定方案（Buildx + BuildKit + Maven）

## 目标
- 代码每次上传后，在服务器端构建时尽量命中缓存：
  - Maven 依赖不重复下载
  - Docker 层尽量复用
- 避免 docker.io metadata/registry 不稳定导致构建超时
- 构建缓存可跨天保留（不依赖 GitHub Runner）

---

## 关键结论
- GitHub Actions Runner 每次是干净环境；本方案的缓存主体位于**服务器端 BuildKit builder（skybuilder）**。
- 构建是否复用 Maven 依赖，取决于：
  1) 每次构建是否使用同一个 buildx builder（skybuilder）
  2) Dockerfile 是否采用 POM 分层 + `--mount=type=cache` 挂载 `/root/.m2`
  3) 构建命令是否避免 `--no-cache` 与频繁 `prune`

---

## 文件 1：GitHub Actions 工作流（.github/workflows/deploy-backend.yml）

```yaml
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

      - name: 上传代码到服务器（排除 .git/target/.idea）
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          source: ".,!.git,!.git/**,!**/target/**,!.idea,!.idea/**"
          target: "/opt/sky/skyBackend"
          overwrite: true

      - name: 重新构建并启动（固定 builder + 不清缓存）
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          command_timeout: 100m
          script: |
            set -euo pipefail

            cd /opt/sky

            # 1) 强制 compose 走 BuildKit/buildx
            export DOCKER_BUILDKIT=1
            export COMPOSE_DOCKER_CLI_BUILD=1

            # 2) 要求 buildkit 配置文件存在（用于 docker.io 镜像源/mirror，避免 registry 超时）
            if [ ! -f /opt/sky/buildkitd.toml ]; then
              echo "ERROR: /opt/sky/buildkitd.toml not found. Please create it first."
              exit 1
            fi

            # 3) 确保 buildx builder 存在且被使用（幂等；不要 rm）
            if ! docker buildx inspect skybuilder >/dev/null 2>&1; then
              docker buildx create --name skybuilder --driver docker-container --use \
                --driver-opt network=host \
                --config /opt/sky/buildkitd.toml \
                --buildkitd-flags '--oci-worker-gc-keepstorage=10737418240'
            else
              docker buildx use skybuilder
            fi
            docker buildx inspect --bootstrap skybuilder >/dev/null

            # 4) 构建并启动（不强制 no-cache；不每次 pull）
            docker compose --progress=plain build --pull=false backend
            docker compose up -d backend
            docker compose ps

            echo "后端部署完成"
```

---

## 文件 2：Dockerfile（/opt/sky/skyBackend/Dockerfile）

```dockerfile
# syntax=docker/dockerfile:1.4

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# 1) 只拷贝 POM（POM 不变就能稳定复用依赖层）
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 2) 预下载依赖（与源码无关）
RUN --mount=type=cache,id=m2repo,target=/root/.m2,sharing=locked \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2 \
    dependency:go-offline

# 3) 再拷贝源码（只影响编译层）
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 4) 编译打包（复用 /root/.m2 缓存）
RUN --mount=type=cache,id=m2repo,target=/root/.m2,sharing=locked \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2 \
    -pl sky-server -am \
    clean package


FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata \
 && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/sky-server/target/sky-server-*.jar /app/app.jar

RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
```

---

## 服务器端一次性准备

### 1) 确认基础镜像已在本机（减少 docker.io 拉取压力）
```bash
docker images | egrep 'maven|eclipse-temurin'
```

### 2) 确认 buildx builder 状态
```bash
docker buildx ls
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}' | grep -i buildkit || true
```

### 3) 确认 compose backend 的 build 配置指向正确 Dockerfile
```bash
cd /opt/sky
docker compose config | sed -n '/^[[:space:]]*backend:/,/^[^[:space:]]/p'
```

目标输出应包含：
- `context: /opt/sky/skyBackend`
- `dockerfile: Dockerfile`

---

## buildkitd.toml（/opt/sky/buildkitd.toml）
- 用于为 BuildKit 配置 registry mirror/拉取策略，降低 docker.io 超时概率
- 内容按实际镜像源配置填写

示例结构（占位）：
```toml
# /opt/sky/buildkitd.toml

[worker.oci]
  gc = true

[registry."docker.io"]
  mirrors = ["https://<mirror-domain>"]
```

---

## 验证方法（每次部署后快速确认）

### 1) 检查 Maven 依赖是否命中缓存
构建日志中以下步骤应为 `CACHED`：
- `dependency:go-offline`
- `clean package` 若仅源码变动，可能重编译；若出现大量 `Downloading from ...` 表示依赖未命中

### 2) 检查 buildx 缓存占用
```bash
docker buildx du --builder skybuilder
docker buildx du --builder skybuilder --verbose | head -n 120
```

---

## 维护规则（确保长期稳定命中）

### 1) 避免删除 builder 与缓存
以下操作会导致缓存丢失或重建成本上升：
- `docker buildx rm skybuilder`
- `docker buildx prune`
- `docker system prune -a`
- 构建时加入 `--no-cache`

### 2) 保持 workflow 的幂等逻辑不变
- builder 不存在才创建
- 存在直接 `docker buildx use skybuilder`
- 始终保持 `DOCKER_BUILDKIT=1` 与 `COMPOSE_DOCKER_CLI_BUILD=1`

### 3) Maven 依赖层的稳定性边界
以下情况属于正常会触发重新拉取/重新计算依赖：
- `pom.xml` 或任一模块 `pom.xml` 发生变化
- 镜像标签发生变化（基础镜像更新到不同 digest）
- `/root/.m2` cache 被手动清理或系统 prune

---

## 常见现象与定位

### 现象 A：出现大量 “Downloading from …”
优先检查：
1) 当前 builder 是否为 skybuilder
```bash
docker buildx ls
```
2) 是否执行过 prune
3) `dependency:go-offline` 是否仍为 `CACHED`

### 现象 B：docker.io metadata 超时（i/o timeout）
优先检查：
1) `/opt/sky/buildkitd.toml` 是否存在且内容正确
2) builder 创建时是否带 `--config /opt/sky/buildkitd.toml`
3) 网络或镜像源可用性

---

## compose 文件清理（去掉 version 警告）
```bash
sudo sed -i '/^[[:space:]]*version:[[:space:]]*.*$/d' /opt/sky/docker-compose.yml
```
