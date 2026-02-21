# Stage 1: Build
FROM maven:3.8.6-eclipse-temurin-17 AS build 
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Note: Because we used WORKDIR /app above, the path to the target folder changes slightly here
COPY --from=build /app/target/parking-backend-0.0.1-SNAPSHOT.jar parking-backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "parking-backend.jar"]