# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 复制 pom 和 settings（配了阿里云镜像）
COPY pom.xml .
COPY settings.xml .

# 用 cache mount 缓存依赖到 /root/.m2，下次构建复用
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s settings.xml dependency:go-offline -B || true

# 复制源码，打包
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s settings.xml clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]