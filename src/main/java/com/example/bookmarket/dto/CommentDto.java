package com.example.bookmarket.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentDto(
        @NotNull(message = "userid can not null")Long userId,
        @NotNull(message = "bookid can not null")Long bookId,
        @NotBlank(message = "کامنت نمیتواند خالی باشد.") String content,
        @Min(1) @Max(5)int rating
) {
}