package com.yashin.messages.dto;
import java.time.OffsetDateTime;
public record Message(long id, long authorId, String text, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
