# ---------------------------
# 1. JDK 21 slim 이미지 사용
# ---------------------------
FROM eclipse-temurin:21-jre

# ---------------------------
# 2. 빌드된 JAR 복사
# ---------------------------
ARG JAR_FILE=build/libs/Fintech-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app.jar

# ---------------------------
# 3. 실행 (환경변수는 .env에서 주입)
# ---------------------------
ENTRYPOINT ["java", "-jar", "/app.jar"]
