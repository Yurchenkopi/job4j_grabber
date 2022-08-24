CREATE TABLE IF NOT EXISTS post (
    id serial PRIMARY KEY,
    name text,
    text text,
    link varchar(255) UNIQUE,
    created timestamp
);