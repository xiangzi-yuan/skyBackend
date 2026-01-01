# syntax=docker/dockerfile:1.6

# ==================== Build stage ====================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# 如果你仓库里有 mvn-settings.xml（镜像源/私服配置），保留这一行；
# 如果没有该文件，请删掉下一行（否则 build 会失败）。
COPY mvn-settings.xml /usr/share/maven/conf/settings.xml

# 1) 先只复制 POM：保证“依赖缓存层”稳定
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 2) 预下载依赖（在线）
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -pl sky-server -am \
    dependency:go-offline

# 3) 再复制源码：你改代码只会影响后面的编译层
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 4) 离线编译（缺依赖直接失败，避免每次都联网刷屏）
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -o \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -pl sky-server -am \
    clean package

# ==================== Runtime stage ====================
FROM eclipse-temurin:17-jre
WORKDIR /app

# 非 root 运行
RUN groupadd -r sky && useradd -r -g sky sky

COPY --from=builder /app/sky-server/target/sky-server-*.jar ./app.jar
RUN chown sky:sky /app/app.jar
USER sky

EXPOSE 8080

# 不装 tzdata，直接让 JVM 按上海时区跑（避免 apt-get 联网）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
