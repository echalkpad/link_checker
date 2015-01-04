FROM eogren/ubuntu_java_base
MAINTAINER Eric Ogren <eogren@gmail.com>

ADD target/status-updater-1.0-SNAPSHOT.jar /status-updater-1.0-SNAPSHOT.jar
ADD config.yml /opt/config/status_updater_config.yml

CMD ["java", "-jar", "status-updater-1.0-SNAPSHOT.jar", "/opt/config/status_updater_config.yml"]
