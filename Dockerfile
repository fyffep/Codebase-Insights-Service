FROM --platform=linux/x86_64 openjdk:11
ARG JAR_FILE=target/codebase-insights-service-1.0-SNAPSHOT.jar
FROM maven:3.6.3 AS maven
RUN mvn clean install
FROM --platform=linux/x86_64 openjdk:11
ARG JAR_FILE=target/codebase-insights-service.jar
COPY ${JAR_FILE} service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/service.jar"]
# CMD [ "/bin/sh","-u","run.sh"]