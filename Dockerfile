FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/marketplace-1.0.0.jar /app/app.jar
EXPOSE 8080
CMD ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -Dspring.datasource.url=\"jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require\" -Dspring.datasource.username=\"${DB_USER}\" -Dspring.datasource.password=\"${DB_PASSWORD}\" -jar /app/app.jar"]
