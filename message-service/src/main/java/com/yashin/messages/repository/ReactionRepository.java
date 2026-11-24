package com.yashin.messages.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class ReactionRepository {
    public record Reaction(long messageId, long userId, String type, OffsetDateTime createdAt) {}
    private final JdbcTemplate jdbc;
    public ReactionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Reaction set(long messageId, long userId, String type) {
        return jdbc.queryForObject("""
                INSERT INTO reactions (message_id, user_id, type) VALUES (?, ?, CAST(? AS reaction_type))
                ON CONFLICT (message_id, user_id) DO UPDATE SET type = EXCLUDED.type
                RETURNING message_id, user_id, type, created_at
                """, (rs, row) -> new Reaction(rs.getLong("message_id"), rs.getLong("user_id"),
                rs.getString("type"), rs.getObject("created_at", OffsetDateTime.class)), messageId, userId, type);
    }
    public boolean remove(long messageId, long userId) {
        return jdbc.update("DELETE FROM reactions WHERE message_id = ? AND user_id = ?", messageId, userId) > 0;
    }
    public Map<String, Long> counts(long messageId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        jdbc.query("SELECT type, count(*) FROM reactions WHERE message_id = ? GROUP BY type ORDER BY type",
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> counts.put(rs.getString(1), rs.getLong(2)), messageId);
        return counts;
    }
    public String own(long messageId, long userId) {
        return jdbc.queryForList("SELECT type::text FROM reactions WHERE message_id = ? AND user_id = ?",
                String.class, messageId, userId).stream().findFirst().orElse(null);
    }
}
