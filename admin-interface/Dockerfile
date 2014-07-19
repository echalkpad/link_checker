FROM ubuntu:14.04

MAINTAINER Eric Ogren <eogren@gmail.com>

# Add core
RUN apt-get update
RUN apt-get install -y software-properties-common python-software-properties

RUN apt-get install -y nginx

# Serve from /var/www/html
ADD docker/nginx_default /etc/nginx/sites-enabled/default
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

EXPOSE 80

# Add the built app to webroot
ADD dist/ /var/www/html/

CMD 'nginx'
