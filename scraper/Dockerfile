FROM eogren/ubuntu_base
MAINTAINER Eric Ogren <eogren@gmail.com>

ADD scraper /root/scraper
ADD scraper_config.yml /opt/config/scraper_config.yml

CMD ["/root/scraper","/opt/config/scraper_config.yml"]
