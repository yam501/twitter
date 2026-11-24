package com.yashin.messages.api;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long total) {
    public static long offset(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(422, "page должен быть >= 0, size — от 1 до 100");
        }
        return (long) page * size;
    }
}
