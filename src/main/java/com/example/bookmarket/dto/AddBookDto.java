package com.example.bookmarket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public record AddBookDto(

        @NotBlank(message = "Title is mandatory") String title,
        @NotBlank(message = "Author is mandatory") String author,
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        @NotNull(message = "Price is mandatory") BigDecimal price,
        Integer numberOfBooks,
        LocalDate publishedDate,
        @NotBlank(message = "Genre is mandatory") String genre,
        Integer volume,
        Integer discountPercentage


) {
}
