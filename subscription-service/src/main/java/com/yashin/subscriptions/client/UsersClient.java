package com.yashin.subscriptions.client;

import com.yashin.subscriptions.api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsersClient {
    public record User(Long id, String firstname, String lastname, List<String> roles) {}
    private final RestClient client;
    public UsersClient(@Value("${services.users-url}") String url) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    public User get(long id) {
        try {
            User user = client.get().uri("/api/v1/users/{id}", id).retrieve().body(User.class);
            if (user == null || user.id() == null || user.roles() == null) {
                throw new ApiException(503, "Некорректный ответ Users");
            }
            return user;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) throw new ApiException(404, "Пользователь не найден");
            throw new ApiException(503, "Сервис Users недоступен");
        } catch (RestClientException e) {
            throw new ApiException(503, "Сервис Users недоступен");
        }
    }
    public List<User> getByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String joined = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        try {
            List<User> users = client.get().uri("/api/v1/users?ids={ids}", joined).retrieve()
                    .body(new ParameterizedTypeReference<List<User>>() {});
            if (users == null || users.stream().anyMatch(u -> u == null || u.id() == null)) {
                throw new ApiException(503, "Некорректный ответ Users");
            }
            return users;
        } catch (RestClientException e) {
            throw new ApiException(503, "Сервис Users недоступен");
        }
    }
}
