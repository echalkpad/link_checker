FROM ubuntu:14.04

MAINTAINER Eric Ogren <eogren@gmail.com>

# Add core
RUN apt-get update
RUN apt-get install -y software-properties-common python-software-properties

# Add java 8
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer oracle-java8-set-default

# Add app
ADD target/service_layer-1.0-SNAPSHOT.jar /service_layer-1.0-SNAPSHOT.jar

EXPOSE 8080
EXPOSE 8081

CMD ["java", "-jar", "service_layer-1.0-SNAPSHOT.jar",  "server"]