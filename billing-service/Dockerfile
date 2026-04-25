FROM eclipse-temurin:17-jre AS runner
WORKDIR /app

# Copy built JAR from builder stage
COPY target/billing-service-0.0.1-SNAPSHOT.jar billing-service.jar

# Expose application ports
EXPOSE 4002
EXPOSE 9001


# Run the application
ENTRYPOINT ["java", "-jar", "billing-service.jar"]
