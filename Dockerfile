FROM gradle:6.7-jdk11

WORKDIR /app
COPY .build/libs/musicbot-1.0.jar /app
CMD ["java","-jar","musicbot-1.0.jar"]