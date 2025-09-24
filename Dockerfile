# 1) Build stage
FROM gradle:8.9-jdk21 AS builder
WORKDIR /workspace
COPY . .
# 캐시 최적화를 원하면 ./gradle, build.gradle 등만 먼저 복사 후 의존성 받아오고 소스 복사
RUN gradle clean bootJar --no-daemon

# 2) Runtime stage
FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ARG JARFILE=/workspace/build/libs/*.jar
COPY --from=builder ${JARFILE} app.jar

# 헬스체크는 필요시 조정
HEALTHCHECK --interval=30s --timeout=5s --retries=5 CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java","-jar","app.jar"]
