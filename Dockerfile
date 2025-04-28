FROM gradle:6.7-jdk11 AS build

WORKDIR /app

COPY build.gradle settings.gradle /app/
COPY gradle /app/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src

RUN gradle build || return 0
COPY . .
RUN gradle clean build

FROM gradle:6.7-jdk11 AS prod

ENV BOT_TOKEN ${BOT_TOKEN}
ENV PREFIX ${PREFIX}
ENV OWNER_ID ${OWNER_ID}
ENV PO_TOKEN ${PO_TOKEN}
ENV VISITOR_DATA ${VISITOR_DATA}

WORKDIR /app
COPY --from=build app/build/libs/musicbot-*-all.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]