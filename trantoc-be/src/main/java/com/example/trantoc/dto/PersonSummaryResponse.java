package com.example.trantoc.dto;

public record PersonSummaryResponse(
        Long id,
        String fullName,
        String aliasName,
        boolean canDelete
) {
}