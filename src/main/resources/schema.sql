CREATE TABLE todo (
    id IDENTITY,
    content VARCHAR(1000),
    done INTEGER
);

CREATE TABLE user_session (
    session_id VARCHAR,
    session_object BINARY,
    expiration_datetime TIMESTAMP NOT NULL,
    PRIMARY KEY (session_id)
);
