FROM eogren/ubuntu_java_base
MAINTAINER Eric Ogren <eogren@gmail.com>

ADD target/crawl-scheduler-1.0-SNAPSHOT.jar /crawl-scheduler-1.0-SNAPSHOT.jar
ADD config.yml /opt/config/crawl_scheduler_config.yml

CMD ["java", "-jar", "crawl-scheduler-1.0-SNAPSHOT.jar", "/opt/config/crawl_scheduler_config.yml"]
