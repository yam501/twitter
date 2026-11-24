package com.yashin.subscriptions.controller;

import com.yashin.subscriptions.api.PageResponse;
import com.yashin.subscriptions.repository.SubscriptionRepository.Subscription;
import com.yashin.subscriptions.security.Auth;
import com.yashin.subscriptions.service.SubscriptionService;
import com.yashin.subscriptions.service.SubscriptionService.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscribers")
public class SubscriptionController {
    private final SubscriptionService service;
    private final Auth auth;
    public SubscriptionController(SubscriptionService service, Auth auth) {
        this.service = service;
        this.auth = auth;
    }
    @PostMapping("/my/{authorId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Subscription subscribe(@PathVariable long authorId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        return service.subscribe(auth.required(header), authorId);
    }
    @DeleteMapping("/my/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable long authorId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        service.unsubscribe(auth.required(header).id(), authorId);
    }
    @GetMapping("/my/{authorId}")
    public Check check(@PathVariable long authorId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        return service.check(auth.required(header).id(), authorId);
    }
    @GetMapping("/my")
    public Map<String, List<Long>> ids(@RequestHeader(value = "Authorization", required = false) String header) {
        return service.authorIds(auth.required(header).id());
    }
    @GetMapping("/users/{userId}/authors")
    public PageResponse<MemberResponse> authors(@PathVariable long userId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.members(userId, true, page, size);
    }
    @GetMapping("/users/{userId}/subscribers")
    public PageResponse<MemberResponse> subscribers(@PathVariable long userId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.members(userId, false, page, size);
    }
    @GetMapping("/users/{userId}/stats")
    public Stats stats(@PathVariable long userId) { return service.stats(userId); }
}
