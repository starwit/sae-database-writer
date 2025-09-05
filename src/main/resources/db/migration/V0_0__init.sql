CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    frame_timestamp TIMESTAMPTZ NOT NULL,
    message_type VARCHAR(255) NOT NULL,
    proto_json JSONB NOT NULL
);