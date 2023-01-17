FROM ubuntu:20.04

# Setup python and java and base system
ENV DEBIAN_FRONTEND noninteractive
ENV LANG=en_US.UTF-8

RUN apt-get update && \
  apt-get install -q -y openjdk-8-jdk python3-pip libsnappy-dev language-pack-en supervisor curl

RUN pip3 install --upgrade pip requests pipenv

ADD supervisord.conf /etc/supervisor/supervisord.conf

