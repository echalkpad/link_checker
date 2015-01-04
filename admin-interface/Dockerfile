FROM eogren/ubuntu_node_base
MAINTAINER Eric Ogren <eogren@gmail.com>

EXPOSE 80
WORKDIR /opt/admin_interface

ADD package.json /opt/admin_interface/package.json
RUN ["npm", "install", "--production"]

ADD server.js /opt/admin_interface/server.js 
ADD config.prod.js /opt/config/admin_interface.config.js
ADD client /opt/admin_interface/client

CMD ["node", "server.js", "/opt/config/admin_interface.config.js"]
