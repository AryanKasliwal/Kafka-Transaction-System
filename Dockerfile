FROM ubuntu:latest
LABEL authors="aryankasliwal"

EXPOSE 8080

ADD target

ENTRYPOINT ["top", "-b"]