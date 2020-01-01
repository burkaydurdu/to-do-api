CREATE TABLE IF NOT EXISTS users(
  id text PRIMARY KEY,
  name text,
  email text NOT NULL UNIQUE,
  password text NOT NULL,
  gender text,
  token text,
  reset_token text);
