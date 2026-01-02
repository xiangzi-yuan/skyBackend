# syntax=docker/dockerfile:1.4

# ============ 第一阶段：构建 ============

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# 1) 只拷贝 POM（只要 POM 不变，这层就能稳定复用）
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 2) 预下载依赖（与源码无关）
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2 \
    dependency:go-offline

# 3) 再拷贝源码（源码变更只影响后续编译，不影响依赖缓存层）
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 4) 编译打包（复用 /root/.m2 缓存）
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -Dmaven.repo.local=/root/.m2 \
    -pl sky-server -am \
    clean package


# ============ 第二阶段：运行时镜像 ============

FROM eclipse-temurin:17-jre
WORKDIR /app

# 基础工具 + 时区
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata \
 && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone \
 && rm -rf /var/lib/apt/lists/*

# 拷贝 jar
COPY --from=builder /build/sky-server/target/sky-server-*.jar /app/app.jar

# 非 root 运行
RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
