# Multi-stage Dockerfile for FreeDoc with SQLite support
# Stage 1: Build Frontend
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

# Copy frontend package files
COPY free-doc-web/package*.json ./

# Install all dependencies (including devDependencies needed for build)
RUN npm ci

# Copy frontend source code
COPY free-doc-web/ ./

# Build frontend
RUN npm run build

# Stage 2: Build Backend
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-builder

WORKDIR /app/backend

# Copy backend pom.xml for dependency caching
COPY free-doc-server/pom.xml ./

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -q

# Copy backend source code
COPY free-doc-server/src ./src

# Copy database schemas to resources directory
COPY db/free_doc_sqlite.sql ./src/main/resources/db/
COPY db/free_doc_mysql.sql ./src/main/resources/db/

# Copy frontend build output to backend static resources
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

# Build backend JAR
RUN mvn clean package -DskipTests -q

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="FreeDoc Team"
LABEL version="1.0.0"
LABEL description="FreeDoc Document Management System with SQLite support"
LABEL org.opencontainers.image.source="https://gitee.com/Tom-shushu/free-doc.git"

WORKDIR /app

# Install wget for health check and dos2unix for line ending fix
RUN apk add --no-cache wget dos2unix

# Create application user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create data, upload and config directories with appropriate permissions
RUN mkdir -p /app/data /app/uploads /app/config && \
    chown -R appuser:appgroup /app

# Copy application JAR from backend builder
COPY --from=backend-builder /app/backend/target/*.jar /app/free-doc-server.jar

# Copy entrypoint script and ensure LF line endings
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN dos2unix /app/docker-entrypoint.sh && \
    chmod +x /app/docker-entrypoint.sh && \
    chown appuser:appgroup /app/docker-entrypoint.sh

# Copy default config file
COPY free-doc.conf /app/config/free-doc.conf
RUN chown appuser:appgroup /app/config/free-doc.conf

# Change ownership of the JAR file
RUN chown appuser:appgroup /app/free-doc-server.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 9200

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9200/api/health || exit 1

# Start application with entrypoint script
ENTRYPOINT ["/app/docker-entrypoint.sh"]
