#############################
# STAGE 1: Frontend build   #
#############################
FROM node:12.16.1-stretch AS frontend-build

# First: Copy only npm stuff so that we don't download the whole internet on every build
COPY frontend/package.json /workspace/frontend/package.json
COPY frontend/package-lock.json /workspace/frontend/package-lock.json
WORKDIR /workspace/frontend

# Install dependencies / dev dependencies
RUN npm ci

# Copy the remaining stuff
COPY . /workspace

# Build app
RUN npm run build-prod

#############################
# STAGE 2: Backend  build   #
#############################
FROM gradle:jdk11 AS backend-build

# prepare workspace
COPY . /workspace
COPY --from=frontend-build /workspace/frontend/dist/mercury-ui/. /workspace/backend/src/main/resources/static
WORKDIR /workspace/backend

# Build backend, don't run tests (they are ran in the cloudbuild.yaml anyway)
RUN gradle --no-daemon build -x test

##############################
# STAGE 3: App package build #
##############################
FROM openjdk:11-jre-slim AS runtime-build

RUN apt-get update; apt-get install -y --no-install-recommends wget; rm -rf /var/lib/apt/lists/*
RUN wget https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.15.0/elastic-apm-agent-1.15.0.jar

COPY --from=backend-build /workspace/backend/build/libs/mercury.jar app.jar

ENTRYPOINT ["java", "-javaagent:/elastic-apm-agent-1.15.0.jar", "-Delastic.apm.service_name=mercury-test", "-Delastic.apm.server_urls=https://2ed1556c8cfd4f7eb285ce4ec5d8d3ad.apm.europe-west3.gcp.cloud.es.io", "-Delastic.apm.application_packages=de.qaware.mercury", "-Djava.security.egd=file:/dev/./urandom", "-XX:+ExitOnOutOfMemoryError", "-Dserver.port=${PORT}","-jar","/app.jar"]	