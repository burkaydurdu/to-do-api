CREATE TABLE IF NOT EXISTS states(
  id text PRIMARY KEY,
  user_id text NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  title text,
  content json,
  created_at timestamp,
  updated_at timestamp,
  all_done boolean DEFAULT false,
  s_order INTEGER);
