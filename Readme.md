# Database writer for Vision API
This repo implements a Valkey/Redis client, that can receive and parse Vision API messages and then writes the serialized messages in JSON format into a Postgres database. It attaches to multiple Valkey streams. Currently, message types `SAE`, `POSITION` and `DETECTION_COUNT` are supported.

On application start the database schema is created / validated using Flyway.

This component is part of the Starwit Awareness Engine (SAE). See umbrella repo here: https://github.com/starwit/starwit-awareness-engine

## Development
- Add vision-api package repository to your Maven config, as described [here](https://github.com/starwit/vision-api#java--maven)
- `deployment` contains a docker compose file that can be used to start the necessary database and valkey instances
- Use sae-tools / a running SAE instance to write interesting messages onto the configured streams
```sh
# Start database and Valkey instance (in deployments)
docker compose up

# Start saedump playback (in starwit-awareness-engine/tools/sae-introspection)
# You can use the short exemplary dumpfile src/test/resources/test_positionsource.saedump
poetry run python play.py --fixed-interval 1s --adjust-timestamps <dumpfile_name>

# Run database-writer (and log messages)
LOGGING_LEVEL_DE_STARWIT=debug mvn spring-boot:run

# Look at database entries arriving
watch "docker compose exec db psql -U postgres database_writer -P pager=off -c 'SELECT * FROM messages order by id desc limit 1'"
```

## Configuration
See `application.properties` for available configuration values.

## Debugging
The log level of the relevant parts can be set through the Spring Boot property `LOGGING_LEVEL_DE_STARWIT`, default is `info`.