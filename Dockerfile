# ==============================================================================
# SmartRail AI - Production Multi-Stage Dockerfile
# Built for Cloud Deployment (Render, Railway, Fly.io, Heroku, AWS AppRunner)
# ==============================================================================

# STAGE 1: Build Java WAR Artifact with Maven & OpenJDK 21
FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder

WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code and SQL datasets
COPY src ./src
COPY sql ./src/main/webapp/WEB-INF/classes/sql/

# Compile and package production WAR
RUN mvn clean package -DskipTests -B

# STAGE 2: Production Tomcat 9 Container
FROM tomcat:9.0.95-jre21-temurin-jammy

LABEL maintainer="SmartRail AI Engineering Team"
LABEL description="SmartRail AI - Tamil Nadu Railway Traffic Operations Control System"

WORKDIR $CATALINA_HOME

# Remove default sample webapps for security & fast startup
RUN rm -rf webapps/*

# Deploy SmartRail AI as ROOT web application (accessible directly at domain root /)
COPY --from=builder /build/target/SmartRailAI.war ./webapps/ROOT.war

# Create persistent data storage directory for H2 file persistence
RUN mkdir -p /data && chmod 777 /data

# Copy and configure custom cloud entrypoint script
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh && chmod +x /usr/local/bin/docker-entrypoint.sh

# Default environment configuration
ENV PORT=8080
ENV SMARTRAIL_H2_PATH=/data/smartrail_db
ENV CATALINA_OPTS="-Xms256m -Xmx768m -Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

