# Database writer for Vision API
This repo implements a Valkey/Redis client, that can receive and parse Vision API messages and then writes the serialized messages in JSON format into a Postgres database. It attaches to multiple Valkey streams. Currently, message types `SAE`, `POSITION` and `DETECTION_COUNT` are supported.

On application start the database schema is created / validated using Flyway.

This component is part of the Starwit Awareness Engine (SAE). See umbrella repo here: https://github.com/starwit/starwit-awareness-engine

## Development
- Add vision-api package repository to your Maven config, as described [here](https://github.com/starwit/vision-api#java--maven)
- `deployment` contains a docker compose file that can be used to start the necessary database and valkey instances
- Use sae-tools / a running SAE instance to write interesting messages onto the configured streams

## Configuration
See `application.properties` for available configuration values.

## Debugging
The log level of the entire application can be set through the environment variable `LOG_LEVEL`, default is `info`.