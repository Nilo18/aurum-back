FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml ./
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

COPY --from=build --chown=app:app --chmod=0444 /build/target/*.jar /opt/aurum/app.jar
RUN chmod 0755 /opt/aurum

ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+ExitOnOutOfMemoryError"

USER app
RUN test -r /opt/aurum/app.jar
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -jar /opt/aurum/app.jar --server.address=0.0.0.0 --server.port=${PORT:-8080}"]
