# ---------- Etapa 1: build com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Baixa as dependências primeiro (fica em cache se só o código mudar)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Etapa 2: só o necessário para rodar (JRE 21) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/zelacidade-api.jar app.jar
# O Render informa a porta pela variável PORT (o application.properties lê ${PORT:8080})
EXPOSE 8080
# Limite de memória pensado no plano gratuito do Render (512 MB)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
