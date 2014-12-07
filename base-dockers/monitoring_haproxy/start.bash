#!/bin/bash

#
# start.bash
#

HAPROXY="/etc/haproxy"
PIDFILE="/var/run/haproxy.pid"

CONFIG="haproxy.cfg"
ERRORS="errors"

cd "$HAPROXY"

exec haproxy -f /etc/haproxy/haproxy.cfg -p "$PIDFILE"
