FROM gradle:8.7-jdk21-alpine AS builder

WORKDIR /build

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN chmod +x gradlew

# cache dependencies
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew bootJar --no-daemon



FROM bellsoft/liberica-openjre-alpine:21

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

USER spring

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

# JVM options optimized for containers
ENV JAVA_OPTS="
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75
-XX:+ExitOnOutOfMemoryError
-XX:+UseG1GC
"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]