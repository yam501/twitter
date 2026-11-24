package com.yashin.subscriptions.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SubscriptionRepository {
    public record Subscription(long subscriberId, long producerId, OffsetDateTime createdAt) {}
    public record Member(long id, OffsetDateTime subscribedAt) {}
    private final JdbcTemplate jdbc;
    public SubscriptionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Subscription add(long userId, long authorId) {
        return jdbc.queryForObject("""
                INSERT INTO subscriptions (user_id, author_id) VALUES (?, ?)
                RETURNING user_id, author_id, created_at
                """, (rs, row) -> new Subscription(rs.getLong("user_id"), rs.getLong("author_id"),
                rs.getObject("created_at", OffsetDateTime.class)), userId, authorId);
    }
    public boolean remove(long userId, long authorId) {
        return jdbc.update("DELETE FROM subscriptions WHERE user_id = ? AND author_id = ?", userId, authorId) > 0;
    }
    public Optional<Subscription> find(long userId, long authorId) {
        return jdbc.query("SELECT * FROM subscriptions WHERE user_id = ? AND author_id = ?",
                (rs, row) -> new Subscription(rs.getLong("user_id"), rs.getLong("author_id"),
                        rs.getObject("created_at", OffsetDateTime.class)), userId, authorId).stream().findFirst();
    }
    public List<Long> authorIds(long userId) {
        return jdbc.queryForList("SELECT author_id FROM subscriptions WHERE user_id = ? ORDER BY author_id",
                Long.class, userId);
    }
    public List<Member> members(long userId, boolean authors, int size, long offset) {
        // Имена колонок выбирает код, клиент передать их не может.
        String target = authors ? "author_id" : "user_id";
        String owner = authors ? "user_id" : "author_id";
        return jdbc.query("SELECT " + target + ", created_at FROM subscriptions WHERE " + owner
                        + " = ? ORDER BY created_at DESC, " + target + " DESC LIMIT ? OFFSET ?",
                (rs, row) -> new Member(rs.getLong(1), rs.getObject(2, OffsetDateTime.class)),
                userId, size, offset);
    }
    public long count(long userId, boolean authors) {
        String owner = authors ? "user_id" : "author_id";
        return jdbc.queryForObject("SELECT count(*) FROM subscriptions WHERE " + owner + " = ?",
                Long.class, userId);
    }
}
