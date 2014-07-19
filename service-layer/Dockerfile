FROM eogren/ubuntu_java_base
MAINTAINER Eric Ogren <eogren@gmail.com>

# Add app
ADD target/service_layer-1.0-SNAPSHOT.jar /service_layer-1.0-SNAPSHOT.jar

EXPOSE 8080
EXPOSE 8081

CMD ["java", "-jar", "service_layer-1.0-SNAPSHOT.jar",  "server"]
