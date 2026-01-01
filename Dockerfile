# ============================================
# 苍穹外卖后端 - Docker 多阶段构建
# ============================================

# ==================== 构建阶段 ====================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 配置 Maven 使用阿里云镜像（国内服务器更稳定）
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?> \
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd"> \
  <mirrors> \
    <mirror> \
      <id>aliyun</id> \
      <mirrorOf>central</mirrorOf> \
      <name>Aliyun Maven Mirror</name> \
      <url>https://maven.aliyun.com/repository/public</url> \
    </mirror> \
  </mirrors> \
</settings>' > /root/.m2/settings.xml

# 第一层：复制所有 pom.xml（利用 Docker 层缓存）
COPY pom.xml ./
COPY sky-common/pom.xml ./sky-common/
COPY sky-pojo/pom.xml ./sky-pojo/
COPY sky-server/pom.xml ./sky-server/

# 下载依赖（这一层会被缓存，pom 不变时不会重新下载）
# 使用 -Dmaven.wagon.http.retryHandler.count=3 增加重试次数
RUN mvn dependency:resolve -B \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    || mvn dependency:resolve -B \
    -Dmaven.wagon.http.retryHandler.count=5 \
    || echo "依赖预下载失败，将在编译阶段重试"

# 第二层：复制源代码
COPY sky-common/src ./sky-common/src
COPY sky-pojo/src ./sky-pojo/src
COPY sky-server/src ./sky-server/src

# 第三层：编译打包（跳过测试）
# 网络重试 + 超时配置
RUN mvn clean package -DskipTests -B \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -Dmaven.artifact.threads=5 \
    -pl sky-server -am

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
