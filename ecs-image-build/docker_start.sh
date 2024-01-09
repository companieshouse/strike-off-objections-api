#!/bin/bash
#
# Start script for docs.developer.ch.gov.uk

exec java -jar -Dserver.port=5009 "strike-off-objections-api.jar"