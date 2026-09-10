FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ENV JAVA_OPTS=""

COPY target/*.jar app.jar

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget -qO- http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
