# syntax=docker/dockerfile:1
ARG UID=1001
ARG VERSION=latest
ARG RELEASE=0

FROM maven:3.9.16-eclipse-temurin-21-alpine AS base

RUN --mount=type=cache,sharing=locked,target=/var/cache/apk \
    apk update && apk add util-linux

FROM base AS build

WORKDIR /app

RUN --mount=type=cache,sharing=locked,target=/root/.m2 \
    --mount=source=pom.xml,target=pom.xml \
    --mount=source=src,target=src \
    script --return --quiet --command "mvn --batch-mode verify" /dev/null | \
      grep -F '[TTY]' # Make sure maven integration test actually prints text '[TTY]'

FROM scratch AS final

ARG UID
COPY --chown=$UID:0 --chmod=775 --from=build /app/target .

FROM build AS release

RUN --mount=type=secret,id=MAVEN_GPG_KEYNAME,env=MAVEN_GPG_KEYNAME \
    --mount=type=secret,id=MAVEN_GPG_PASSPHRASE,env=MAVEN_GPG_PASSPHRASE \
    --mount=type=cache,sharing=locked,target=/root/.m2 \
    --mount=source=pom.xml,target=pom.xml \
    --mount=source=src,target=src \
    mvn --batch-mode --activate-profiles=release -Dgpg.keyname="$$MAVEN_GPG_KEYNAME" deploy
