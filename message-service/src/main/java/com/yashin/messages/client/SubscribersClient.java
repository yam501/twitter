package com.yashin.messages.client;

import com.yashin.messages.api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import java.util.List;

@Component
public class SubscribersClient {
    public record Authors(List<Long> authorIds) {}
    private final RestClient client;
    public SubscribersClient(@Value("${services.subscribers-url}") String url) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    public List<Long> authorIds(String header) {
        try {
            Authors response = client.get().uri("/api/v1/subscribers/my").header("Authorization", header)
                    .retrieve().body(Authors.class);
            if (response == null || response.authorIds() == null ||
                    response.authorIds().stream().anyMatch(id -> id == null || id <= 0)) {
                throw new ApiException(503, "Некорректный ответ сервиса подписок");
            }
            return response.authorIds();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) throw new ApiException(401, "Токен отклонён сервисом подписок");
            throw new ApiException(503, "Сервис подписок недоступен");
        } catch (RestClientException e) {
            throw new ApiException(503, "Сервис подписок недоступен");
        }
    }
}
