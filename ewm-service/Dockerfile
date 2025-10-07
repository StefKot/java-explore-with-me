FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY target/*.jar ewm-service.jar
ENTRYPOINT ["java","-jar","/ewm-service.jar"]