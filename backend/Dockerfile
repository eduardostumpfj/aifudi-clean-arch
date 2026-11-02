# ================ BUILD DO PROJETO ======================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean install -DskipTests


# ================ EXECUÇÃO ======================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.war app.war

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE ${BACKEND_PORT}

ENTRYPOINT ["java", "-jar", "app.war"]