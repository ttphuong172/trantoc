package com.example.trantoc.dto;

public record ParentResponse(
        PersonSummaryResponse father,
        PersonSummaryResponse mother
) {
}