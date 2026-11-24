package com.yashin.messages.repository;

import com.yashin.messages.dto.Message;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MessageRepository {
    public record ListItem(long id, long authorId, String text, OffsetDateTime createdAt, long reactionsCount) {}
    private final JdbcTemplate jdbc;
    private static final RowMapper<Message> MAPPER = (rs, row) -> new Message(rs.getLong("id"),
            rs.getLong("author_id"), rs.getString("text"), rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class));
    private static final RowMapper<ListItem> LIST_MAPPER = (rs, row) -> new ListItem(rs.getLong("id"),
            rs.getLong("author_id"), rs.getString("text"), rs.getObject("created_at", OffsetDateTime.class),
            rs.getLong("reactions_count"));

    public MessageRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Message create(long authorId, String text) {
        return jdbc.queryForObject("INSERT INTO messages (author_id, text) VALUES (?, ?) RETURNING *",
                MAPPER, authorId, text);
    }
    public Optional<Message> find(long id) {
        return jdbc.query("SELECT * FROM messages WHERE id = ?", MAPPER, id).stream().findFirst();
    }
    // FOR UPDATE согласует изменение/удаление сообщения с изменением его реакций.
    public Optional<Message> lock(long id) {
        return jdbc.query("SELECT * FROM messages WHERE id = ? FOR UPDATE", MAPPER, id).stream().findFirst();
    }
    public Message update(long id, String text) {
        return jdbc.queryForObject("UPDATE messages SET text = ?, updated_at = now() WHERE id = ? RETURNING *",
                MAPPER, text, id);
    }
    public void delete(long id) { jdbc.update("DELETE FROM messages WHERE id = ?", id); }

    public List<ListItem> list(List<Long> authors, int size, long offset) {
        if (authors.isEmpty()) return List.of();
        return jdbc.query(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    SELECT m.*, (SELECT count(*) FROM reactions r WHERE r.message_id = m.id) AS reactions_count
                    FROM messages m WHERE m.author_id = ANY (?)
                    ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?
                    """);
            statement.setArray(1, connection.createArrayOf("bigint", authors.toArray(Long[]::new)));
            statement.setInt(2, size);
            statement.setLong(3, offset);
            return statement;
        }, LIST_MAPPER);
    }
    public long count(List<Long> authors) {
        if (authors.isEmpty()) return 0;
        return jdbc.query(connection -> {
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM messages WHERE author_id = ANY (?)");
            statement.setArray(1, connection.createArrayOf("bigint", authors.toArray(Long[]::new)));
            return statement;
        }, (rs, row) -> rs.getLong(1)).getFirst();
    }
}
