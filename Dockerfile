# syntax=docker/dockerfile:1.4

# ============================================
# 苍穹外卖后端 - Docker 多阶段构建（最终版）
# - BuildKit cache 复用 /root/.m2
# - 限制 Maven 内存，降低 2G 机器构建失败概率
# ============================================

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Maven 编译期内存控制（关键：2G 机器不控内存容易炸）
ENV MAVEN_OPTS="-Xms128m -Xmx768m -XX:+UseG1GC -Dfile.encoding=UTF-8"

# Maven 镜像源
COPY mvn-settings.xml /usr/share/maven/conf/settings.xml

# 只拷 pom（用于依赖层缓存）
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 第一步：预下载依赖（写入 /root/.m2 缓存）
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -DskipTests \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -Dmaven.artifact.threads=2 \
    -pl sky-server -am \
    dependency:go-offline

# 再拷源码（源码变只影响后面编译层）
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 第二步：编译打包
# 这里不强制 -o（离线）是为了避免某些插件/父 POM 没被 go-offline 拉全导致失败；
# 但依赖会优先走 cache，不会每次重新下。
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -DskipTests \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -Dmaven.artifact.threads=2 \
    -pl sky-server -am \
    clean package

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r sky && useradd -r -g sky sky

COPY --from=builder /app/sky-server/target/sky-server-*.jar ./app.jar
RUN chown sky:sky /app/app.jar
USER sky

EXPOSE 8080

# 运行期 JVM 内存控制（容器里也别太大）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
