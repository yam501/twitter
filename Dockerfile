FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /project
COPY pom.xml .
COPY user-service user-service
COPY subscription-service subscription-service
COPY message-service message-service
ARG MODULE
RUN mvn -B -pl ${MODULE} -am package -DskipTests
RUN cp ${MODULE}/target/*.jar /application.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /application.jar app.jar
USER 10001
ENTRYPOINT ["java", "-jar", "app.jar"]
