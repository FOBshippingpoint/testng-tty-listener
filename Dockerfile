# syntax=docker/dockerfile:1
FROM maven:3.9.16-eclipse-temurin-21-alpine AS base

RUN --mount=type=cache,sharing=locked,target=/var/cache/apk \
    apk update && apk add util-linux

FROM base AS build

WORKDIR /app

RUN --mount=type=cache,sharing=locked,target=/root/.m2 \
    --mount=source=pom.xml,target=pom.xml \
    --mount=source=src,target=src \
    script --return --quiet --command "mvn -Prun-its verify" /dev/null
