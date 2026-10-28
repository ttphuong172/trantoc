package com.example.trantoc.dto;

import com.example.trantoc.entity.FamilyRole;
import com.example.trantoc.entity.Gender;

public record PersonDetailResponse(
        Long id,
        String fullName,
        String aliasName,
        Integer generation,
        Integer birthYear,
        Integer deathYear,
        String notes,
        Gender gender,
        String address,
        String burialPlace,
        String deathAnniversary,
        Boolean rootAncestor,
        FamilyRole role
) {
}