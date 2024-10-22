#!/bin/sh

# Create the logfile.log required by Fluentd
touch /app/logfile.log

# Start Supervisor to manage services
exec /usr/local/bin/supervisord -c /etc/supervisord.conf
