-- Compatibility fixture: the schema shipped in T4C-0161, before accounts and leases.
CREATE TABLE online_schema (version integer PRIMARY KEY, installed_at timestamptz NOT NULL DEFAULT now());
INSERT INTO online_schema(version) VALUES (1);
CREATE TABLE online_characters (
  id uuid PRIMARY KEY, account_id uuid NOT NULL,
  name varchar(32) NOT NULL CHECK (length(name) BETWEEN 1 AND 32),
  world integer NOT NULL CHECK (world >= 0),
  x double precision NOT NULL CHECK (x >= 0 AND x < 'Infinity'::float8),
  y double precision NOT NULL CHECK (y >= 0 AND y < 'Infinity'::float8),
  gold bigint NOT NULL DEFAULT 0 CHECK (gold >= 0),
  experience bigint NOT NULL DEFAULT 0 CHECK (experience >= 0),
  revision bigint NOT NULL DEFAULT 0 CHECK (revision >= 0),
  updated_at timestamptz NOT NULL DEFAULT now());
CREATE INDEX online_characters_account ON online_characters(account_id);
CREATE TABLE online_rewards (
  character_id uuid NOT NULL REFERENCES online_characters(id), event_id uuid NOT NULL,
  gold bigint NOT NULL CHECK (gold >= 0), experience bigint NOT NULL CHECK (experience >= 0),
  created_at timestamptz NOT NULL DEFAULT now(), PRIMARY KEY(character_id, event_id));
