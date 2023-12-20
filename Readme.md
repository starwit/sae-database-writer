# Database writer for Vision API
This repo implements a JMS and Redis client, that can receive and parse Vision API messages and then writes into a Postgres database.

This component is part of the Starwit Awareness Engine (SAE). See umbrella repo here: https://github.com/starwit/vision-pipeline-k8s

## Development
- Add vision-api package repository to your Maven config, as described [here](https://github.com/starwit/vision-api#java--maven)

## Configuration
Configuration is read by the dotenv library. To that end, the `.env.template` contains all possible configuration values. In order for the application to read the configuration values, the `.env` file has to be in the working directory of the JVM process. 
All contained configuration parameters can be set / overwritten by setting an environment variable of the same name, which is the preferred way for container deployment.

## Debugging
The log level of the entire application can be set through the environment variable `LOG_LEVEL`, default is `info`.