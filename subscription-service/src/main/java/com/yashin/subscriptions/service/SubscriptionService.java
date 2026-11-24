package com.yashin.subscriptions.service;

import com.yashin.subscriptions.api.ApiException;
import com.yashin.subscriptions.api.PageResponse;
import com.yashin.subscriptions.client.UsersClient;
import com.yashin.subscriptions.repository.SubscriptionRepository;
import com.yashin.subscriptions.repository.SubscriptionRepository.Subscription;
import com.yashin.subscriptions.security.Actor;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
    public record Check(boolean subscribed, OffsetDateTime createdAt) {}
    public record MemberResponse(long id, String firstname, String lastname, OffsetDateTime subscribedAt) {}
    public record Stats(long subscribersCount, long subscriptionsCount) {}

    private final SubscriptionRepository repository;
    private final UsersClient users;
    public SubscriptionService(SubscriptionRepository repository, UsersClient users) {
        this.repository = repository;
        this.users = users;
    }
    public Subscription subscribe(Actor actor, long authorId) {
        actor.requireRole("subscriber");
        if (actor.id() == authorId) throw new ApiException(422, "Нельзя подписаться на себя");
        UsersClient.User author = users.get(authorId);
        if (!author.roles().contains("producer")) throw new ApiException(422, "У автора нет роли producer");
        if (repository.find(actor.id(), authorId).isPresent()) throw new ApiException(409, "Подписка уже существует");
        return repository.add(actor.id(), authorId);
    }
    public void unsubscribe(long userId, long authorId) {
        if (!repository.remove(userId, authorId)) throw new ApiException(404, "Подписка не найдена");
    }
    public Check check(long userId, long authorId) {
        users.get(authorId);
        return repository.find(userId, authorId).map(s -> new Check(true, s.createdAt()))
                .orElse(new Check(false, null));
    }
    public PageResponse<MemberResponse> members(long userId, boolean authors, int page, int size) {
        long offset = PageResponse.offset(page, size);
        var rows = repository.members(userId, authors, size, offset);
        Map<Long, UsersClient.User> profiles = users.getByIds(rows.stream().map(SubscriptionRepository.Member::id).toList())
                .stream().collect(Collectors.toMap(UsersClient.User::id, Function.identity()));
        var items = rows.stream().map(row -> {
            UsersClient.User user = profiles.get(row.id());
            if (user == null) throw new ApiException(503, "Users не вернул профиль участника подписки");
            return new MemberResponse(row.id(), user.firstname(), user.lastname(), row.subscribedAt());
        }).toList();
        return new PageResponse<>(items, page, size, repository.count(userId, authors));
    }
    public Stats stats(long userId) {
        return new Stats(repository.count(userId, false), repository.count(userId, true));
    }
    public Map<String, java.util.List<Long>> authorIds(long userId) {
        return Map.of("authorIds", repository.authorIds(userId));
    }
}
