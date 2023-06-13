#!/bin/bash

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

docker build -t ghcr.io/starwit/vision-api-jms-client:$VERSION .