FROM gradle:jdk11 as build
LABEL maintainer="Noah Sandman <noah@modulytic.com>"

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

# Run
FROM openjdk:11-jre-slim
ARG DALIA_VERSION="1.0-SNAPSHOT"

# Extract executable
COPY --from=build /home/gradle/src/build/distributions/dalia-${DALIA_VERSION}.tar /dalia.tar
RUN tar -xf dalia.tar -C /
RUN mv /dalia-${DALIA_VERSION} /app

# Copy config files
COPY conf /app/prefix

EXPOSE 80 2775
ENTRYPOINT ["/app/bin/dalia"]
