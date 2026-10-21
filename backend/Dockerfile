FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY flowseque-1.0.0.jar app.jar
EXPOSE 19001
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
