#!/bin/bash

# We cannot run mvn help:evaluate directly because of this bug: https://stackoverflow.com/q/77000250/11338899
VERSION=$(docker run -it --rm -v $(pwd)/pom.xml:/pom.xml -w / maven:3.6-openjdk-17-slim mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

docker push docker.internal.starwit-infra.de/sae/vision-api-jms-client:$VERSION