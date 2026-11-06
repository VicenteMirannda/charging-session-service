# Stage 1: build the jar with Maven
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B package -DskipTests

# Stage 2: run it on a smaller image that only has the JRE
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/charging-session-service-*.jar app.jar
EXPOSE 8012
ENTRYPOINT ["java", "-jar", "app.jar"]
