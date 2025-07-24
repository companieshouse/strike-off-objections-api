#!/bin/bash
#
# Start script for strike-off-objections-api

exec java -jar -Dserver.port=5009 -XX:MaxRAMPercentage=80 "strike-off-objections-api.jar"