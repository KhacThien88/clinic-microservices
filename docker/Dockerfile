FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

ARG ARTIFACT_NAME
ENV ARTIFACT_NAME=${ARTIFACT_NAME}

RUN test -n "$ARTIFACT_NAME"

COPY ${ARTIFACT_NAME}.jar application.jar

ENTRYPOINT ["java", "-jar", "/app/application.jar", "--spring.config.location=/app/config/application.properties"]