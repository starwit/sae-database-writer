#!/bin/bash

# We cannot run mvn help:evaluate directly because of this bug: https://stackoverflow.com/q/77000250/11338899
VERSION=$(docker run -it --rm -v $(pwd)/pom.xml:/pom.xml -w / maven:3.6-openjdk-17-slim mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

mvn clean package

docker build -t starwitorg/sae-database-writer:$VERSION .