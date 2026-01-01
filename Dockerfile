# syntax=docker/dockerfile:1.4

# ============================================
# 苍穹外卖后端 - Docker 多阶段构建（可缓存依赖）
# ============================================

FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Maven 镜像源（如果你仓库里有这个文件）
COPY mvn-settings.xml /usr/share/maven/conf/settings.xml

# 只拷 pom：让“依赖层”尽量不被源码改动影响
COPY pom.xml pom.xml
COPY sky-common/pom.xml sky-common/pom.xml
COPY sky-pojo/pom.xml sky-pojo/pom.xml
COPY sky-server/pom.xml sky-server/pom.xml

# 1) 预下载依赖（可联网；缓存到 BuildKit cache）
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -Dmaven.artifact.threads=2 \
    -pl sky-server -am \
    dependency:go-offline

# 再拷源码（源码变动只影响后面编译层）
COPY sky-common sky-common
COPY sky-pojo sky-pojo
COPY sky-server sky-server

# 2) 离线编译（不允许下载；缺依赖就直接失败）
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests -o \
    -s /usr/share/maven/conf/settings.xml \
    -Dmaven.repo.local=/root/.m2 \
    -Dmaven.artifact.threads=2 \
    -pl sky-server -am \
    clean package

# ================ 运行阶段 ================
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r sky && useradd -r -g sky sky
COPY --from=builder /app/sky-server/target/sky-server-*.jar ./app.jar
RUN chown sky:sky /app/app.jar
USER sky

EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
