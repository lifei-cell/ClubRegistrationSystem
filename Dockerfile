FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN apk add --no-cache tzdata musl-locales musl-locales-lang && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制 jar 包
COPY target/app.jar app.jar

EXPOSE 8080

# 设置 locale 为 UTF-8
ENV LANG=zh_CN.UTF-8 \
    LC_ALL=zh_CN.UTF-8

# 使用 exec 形式，让 Java 进程作为 PID 1 以正确接收信号
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
