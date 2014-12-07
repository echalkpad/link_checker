FROM ubuntu:14.04

MAINTAINER Eric Ogren <eogren@gmail.com>

RUN apt-get update && apt-get install -y haproxy && sed -i 's/^ENABLED=.*/ENABLED=1/' /etc/default/haproxy && \
  rm -rf /var/lib/apt/lists/*

ADD haproxy.cfg /etc/haproxy/haproxy.cfg
ADD start.bash /haproxy-start

WORKDIR /etc/haproxy
CMD [ "bash", "/haproxy-start" ]

EXPOSE 80
EXPOSE 9999

