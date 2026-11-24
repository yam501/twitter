DO $$ BEGIN
    CREATE TYPE reaction_type AS ENUM ('like', 'dislike', 'heart', 'laugh', 'sad');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$^^

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_messages_text_length CHECK (char_length(text) BETWEEN 1 AND 2000)
)^^
CREATE TABLE IF NOT EXISTS reactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    type reaction_type NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_reactions_one_per_user UNIQUE (message_id, user_id)
)^^
CREATE INDEX IF NOT EXISTS idx_messages_author_created ON messages (author_id, created_at DESC, id DESC)^^
