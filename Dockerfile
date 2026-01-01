# syntax=docker/dockerfile:1.4

# ============================================
# 苍穹外卖后端 - Docker 多阶段构建
# ============================================

# ==================== 构建阶段 ====================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 用文件方式配置阿里云源
COPY mvn-settings.xml /usr/share/maven/conf/settings.xml

# 先 copy pom（尽量利用层缓存）
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 再 copy 源码
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 关键：持久化 Maven 本地仓库缓存 + 失败重试参数 + 降低并发
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.artifact.threads=2 \
    clean package -pl sky-server -am

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建非 root 用户
RUN groupadd -r sky && useradd -r -g sky sky

# 从构建阶段复制 jar
COPY --from=builder /app/sky-server/target/sky-server-*.jar ./app.jar

# 设置文件权限
RUN chown sky:sky /app/app.jar

# 切换到非 root 用户
USER sky

# 暴露端口
EXPOSE 8080

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 启动应用（使用 prod profile）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
