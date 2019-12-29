CREATE TABLE IF NOT EXISTS users(
  id text PRIMARY KEY,
  name character(50),
  email character(100) NOT NULL UNIQUE,
  password text NOT NULL,
  gender character(20),
  token text,
  reset_token text);
