FROM openjdk:11
ARG JAR_FILE=target/codebase-insights-service-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/service.jar"]