FROM eclipse-temurin:21-jre-alpine

LABEL authors="B00tLoad_"

RUN mkdir -p /opt/app
RUN mkdir -p /data/b00tload-tools/snowflake

COPY target/SnowflakeService-jar-with-dependencies.jar /opt/app
CMD ["java", "-jar", "/opt/app/SnowflakeService-jar-with-dependencies.jar", "--docker"]