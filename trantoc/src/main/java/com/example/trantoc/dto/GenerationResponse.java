package com.example.trantoc.dto;

public record GenerationResponse(
        Long personId,
        String fullName,
        Integer generation
) {
}