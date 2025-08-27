# Amazon Corretto 21 기반 런타임 이미지
FROM amazoncorretto:21

# 작업 디렉토리 생성
WORKDIR /app

# Spring Boot JAR 복사
COPY ./build/libs/*-SNAPSHOT.jar app.jar

# 컨테이너 포트 오픈
EXPOSE 8080

# Java Agent 설정 포함 ENTRYPOINT
ENTRYPOINT ["java", "-jar", "app.jar"]
