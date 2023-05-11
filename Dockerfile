FROM openjdk:20
LABEL authors="aryankasliwal"

EXPOSE 8080

ADD target/transactions-server-docker.jar transactions-server-docker.jar

ENTRYPOINT ["java", "-jar", "/transactions-server-docker.jar"]