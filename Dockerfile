#
# Build
#
FROM maven:3.9.7-eclipse-temurin-21-alpine AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN mvn -f $HOME/pom.xml clean compile assembly:single


#
# Package
#
FROM eclipse-temurin:21-jre-alpine

LABEL authors="B00tLoad_"
ARG JAR_FILE=/usr/app/target/*.jar
RUN apk --no-cache add curl
RUN mkdir -p /opt/app
RUN mkdir -p /data/b00tload-tools/snowflake
HEALTHCHECK CMD curl -f http://localhost:9567/health || exit 1
COPY --from=build $JAR_FILE /opt/app
CMD ["java", "-jar", "/opt/app/SnowflakeService-jar-with-dependencies.jar", "--docker"]