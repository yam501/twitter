package com.yashin.messages.service;

import com.yashin.messages.api.ApiException;
import com.yashin.messages.api.PageResponse;
import com.yashin.messages.client.UsersClient;
import com.yashin.messages.client.SubscribersClient;
import com.yashin.messages.dto.Message;
import com.yashin.messages.repository.MessageRepository;
import com.yashin.messages.repository.MessageRepository.ListItem;
import com.yashin.messages.repository.ReactionRepository;
import com.yashin.messages.repository.ReactionRepository.Reaction;
import com.yashin.messages.security.Actor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MessageService {
    public record Details(long id, long authorId, String text, OffsetDateTime createdAt,
                          OffsetDateTime updatedAt, Map<String, Long> reactions) {}
    public record ReactionSummary(long messageId, long total, Map<String, Long> byType, String myReaction) {}
    private static final Set<String> REACTION_TYPES = Set.of("like", "dislike", "heart", "laugh", "sad");
    private final MessageRepository messages;
    private final ReactionRepository reactions;
    private final UsersClient users;
    private final SubscribersClient subscribers;

    public MessageService(MessageRepository messages, ReactionRepository reactions,
                          UsersClient users, SubscribersClient subscribers) {
        this.messages = messages;
        this.reactions = reactions;
        this.users = users;
        this.subscribers = subscribers;
    }
    public Message create(Actor actor, String text) {
        actor.requireRole("producer");
        return messages.create(actor.id(), validText(text));
    }
    public Details get(long id) {
        Message message = find(id);
        return new Details(message.id(), message.authorId(), message.text(), message.createdAt(),
                message.updatedAt(), reactions.counts(id));
    }
    @Transactional
    public Message edit(Actor actor, long id, String text) {
        Message message = lock(id);
        if (message.authorId() != actor.id()) throw new ApiException(403, "Изменять сообщение может только автор");
        return messages.update(id, validText(text));
    }
    @Transactional
    public void delete(Actor actor, long id) {
        Message message = lock(id);
        if (message.authorId() != actor.id() && !actor.isAdmin()) {
            throw new ApiException(403, "Удалять сообщение может автор или администратор");
        }
        messages.delete(id);
    }
    public PageResponse<ListItem> byAuthor(long authorId, int page, int size) {
        PageResponse.offset(page, size);
        users.get(authorId);
        return page(List.of(authorId), page, size);
    }
    public PageResponse<ListItem> feed(String header, int page, int size) {
        PageResponse.offset(page, size);
        return page(subscribers.authorIds(header), page, size);
    }
    @Transactional
    public Reaction react(Actor actor, long id, String type) {
        lock(id);
        if (type == null || !REACTION_TYPES.contains(type)) throw new ApiException(422, "Неизвестный тип реакции");
        return reactions.set(id, actor.id(), type);
    }
    @Transactional
    public void removeReaction(Actor actor, long id) {
        lock(id);
        if (!reactions.remove(id, actor.id())) throw new ApiException(404, "Реакция не найдена");
    }
    public ReactionSummary reactions(long id, Actor actor) {
        find(id);
        Map<String, Long> counts = reactions.counts(id);
        return new ReactionSummary(id, counts.values().stream().mapToLong(Long::longValue).sum(),
                counts, actor == null ? null : reactions.own(id, actor.id()));
    }
    private PageResponse<ListItem> page(List<Long> authors, int page, int size) {
        long offset = PageResponse.offset(page, size);
        return new PageResponse<>(messages.list(authors, size, offset), page, size, messages.count(authors));
    }
    private Message find(long id) {
        return messages.find(id).orElseThrow(() -> new ApiException(404, "Сообщение не найдено"));
    }
    private Message lock(long id) {
        return messages.lock(id).orElseThrow(() -> new ApiException(404, "Сообщение не найдено"));
    }
    private String validText(String text) {
        if (text == null) throw new ApiException(400, "Поле text обязательно");
        String value = text.strip();
        int length = value.codePointCount(0, value.length());
        if (value.isBlank() || length > 2000) throw new ApiException(422, "Текст должен содержать от 1 до 2000 символов");
        return value;
    }
}
