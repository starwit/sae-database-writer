CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    message_timestamp TIMESTAMPTZ NOT NULL,
    stream_key VARCHAR(255) NOT NULL,
    message_type VARCHAR(255) NOT NULL,
    proto_json JSONB NOT NULL
);