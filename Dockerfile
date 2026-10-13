# ---- Stage 1: build the jar with Maven and the full JDK ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom first so dependency downloads are cached between builds
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---- Stage 2: run it on a small JRE-only image ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Don't run as root inside the container
RUN useradd --system --uid 1001 appuser
USER appuser

COPY --from=build /app/target/ledger-api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
