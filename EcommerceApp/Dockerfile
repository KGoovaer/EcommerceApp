# ---- Stage 1: Build WAR ----
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first for better layer caching
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -q -DskipTests

# ---- Stage 2: Run on Tomcat ----
FROM tomcat:9-jdk11-temurin

# Remove the default Tomcat ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/*

# Pre-extract the WAR into ROOT/ so the volume mount on ROOT/images
# doesn't prevent Tomcat from seeing the app files (Tomcat skips WAR
# extraction when the destination directory already exists at startup).
COPY --from=build /app/target/EcommerceApp.war /tmp/app.war
RUN mkdir -p /usr/local/tomcat/webapps/ROOT && \
    cd /usr/local/tomcat/webapps/ROOT && \
    jar -xf /tmp/app.war && \
    rm /tmp/app.war

# SQLite DB lives in /data (bind-mounted from host at runtime)
# Uploaded product images live in the deployed webapp's images/ dir (named volume)
ENV SQLITE_DB_PATH=/data/mydatabase.db
ENV UPLOAD_PATH=/usr/local/tomcat/webapps/ROOT/

EXPOSE 8080
