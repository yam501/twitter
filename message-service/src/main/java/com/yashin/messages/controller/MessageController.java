package com.yashin.messages.controller;

import com.yashin.messages.api.PageResponse;
import com.yashin.messages.dto.Message;
import com.yashin.messages.repository.MessageRepository.ListItem;
import com.yashin.messages.repository.ReactionRepository.Reaction;
import com.yashin.messages.security.Auth;
import com.yashin.messages.service.MessageService;
import com.yashin.messages.service.MessageService.Details;
import com.yashin.messages.service.MessageService.ReactionSummary;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    public record TextRequest(String text) {}
    public record ReactionRequest(String type) {}
    private final MessageService service;
    private final Auth auth;
    public MessageController(MessageService service, Auth auth) {
        this.service = service;
        this.auth = auth;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message create(@RequestBody TextRequest body,
            @RequestHeader(value = "Authorization", required = false) String header) {
        return service.create(auth.required(header), body.text());
    }
    @GetMapping("/{id}")
    public Details get(@PathVariable long id) { return service.get(id); }
    @PatchMapping("/{id}")
    public Message edit(@PathVariable long id, @RequestBody TextRequest body,
            @RequestHeader(value = "Authorization", required = false) String header) {
        return service.edit(auth.required(header), id, body.text());
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        service.delete(auth.required(header), id);
    }
    @GetMapping
    public PageResponse<ListItem> byAuthor(@RequestParam long authorId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.byAuthor(authorId, page, size);
    }
    @GetMapping("/feed")
    public PageResponse<ListItem> feed(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String header) {
        auth.required(header);
        return service.feed(header, page, size);
    }
    @PutMapping("/{id}/reactions")
    public Reaction react(@PathVariable long id, @RequestBody ReactionRequest body,
            @RequestHeader(value = "Authorization", required = false) String header) {
        return service.react(auth.required(header), id, body.type());
    }
    @DeleteMapping("/{id}/reactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(@PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        service.removeReaction(auth.required(header), id);
    }
    @GetMapping("/{id}/reactions")
    public ReactionSummary reactions(@PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        return service.reactions(id, auth.optional(header));
    }
}
