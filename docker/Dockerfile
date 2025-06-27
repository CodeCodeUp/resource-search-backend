FROM openjdk:8-jre-alpine

WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 复制jar文件
COPY resource-search-backend-1.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 9090

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
