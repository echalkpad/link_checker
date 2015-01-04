FROM eogren/ubuntu_java_base
MAINTAINER Eric Ogren <eogren@gmail.com>

ADD service-layer-service/target/service-layer-service-1.0-SNAPSHOT.jar /service-layer-service-1.0-SNAPSHOT.jar
ADD server.yml /opt/config/data_api_config.yml
EXPOSE 8080
EXPOSE 8081

CMD ["java", "-jar", "service-layer-service-1.0-SNAPSHOT.jar",  "server", "/opt/config/data_api_config.yml"]
