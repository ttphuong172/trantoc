package com.example.trantoc.dto;

import com.example.trantoc.entity.FamilyRole;

import java.util.List;

public record PersonListResponse(
        Long id,
        String fullName,
        String aliasName,
        Integer generation,

        String fatherName,
        String fatherAliasName,

        String motherName,
        String motherAliasName,

        String spouseName,
        String spouseAliasName,
        FamilyRole role,

        List<String>children
) {
}
