#!/bin/bash
mvn clean package

docker build -t starwitorg/sae-database-writer:local .