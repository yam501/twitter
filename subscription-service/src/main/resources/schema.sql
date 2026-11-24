CREATE TABLE IF NOT EXISTS subscriptions (
    user_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, author_id),
    CONSTRAINT chk_no_self_subscribe CHECK (user_id <> author_id)
);
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_created ON subscriptions (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_subscriptions_author_created ON subscriptions (author_id, created_at DESC);
