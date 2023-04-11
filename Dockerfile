FROM gradle:6.7-jdk11

ENV BOT_TOKEN ${BOT_TOKEN}
ENV PREFIX ${PREFIX}
ENV OWNER_ID ${OWNER_ID}

WORKDIR /app
COPY ./build/libs/musicbot-1.0-all.jar /app
CMD ["java","-jar","musicbot-1.0-all.jar"]