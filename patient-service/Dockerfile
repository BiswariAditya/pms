FROM eclipse-temurin:17-jre AS runner
WORKDIR /app

COPY target/patient-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4001
ENTRYPOINT ["java","-jar","app.jar"]
