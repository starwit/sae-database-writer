#!/bin/bash

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

docker push ghcr.io/starwit/vision-api-jms-client:$VERSION