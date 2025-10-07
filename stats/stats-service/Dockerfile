FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY target/*.jar stats.jar
ENTRYPOINT ["java","-jar","/stats.jar"]