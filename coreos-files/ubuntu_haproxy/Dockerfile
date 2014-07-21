FROM eogren/ubuntu_base
MAINTAINER Eric Ogren <eogren@gmail.com>

# SupervisorD / HAProxy
RUN apt-get install -y supervisor haproxy

ADD files/supervisor.conf /etc/supervisor/conf.d/supervisord.conf
ADD files/haproxy.conf /etc/haproxy/haproxy.cfg
ADD files/haproxy.supervisor.conf /etc/supervisor/conf.d/haproxy.conf

# Confd
ADD files/confd-0.5.0-linux-amd64 /usr/local/bin/confd
ADD files/confd.haproxy.toml /etc/confd/conf.d/haproxy.toml
ADD files/confd.supervisor.conf /etc/supervisor/conf.d/confd.conf
ADD files/haproxy.conf.tmpl /etc/confd/templates/haproxy.conf.tmpl
ADD files/confd.toml /etc/confd/confd.toml

# Changeme
EXPOSE 8080

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]

