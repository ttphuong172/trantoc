package com.example.trantoc.dto;

import com.example.trantoc.entity.Gender;

public record ChildResponse(
        Long id,
        String fullName,
        String aliasName,
        Gender gender,
        Integer childOrder,
        String motherName
) {
}