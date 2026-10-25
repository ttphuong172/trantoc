package com.example.trantoc.dto;

import com.example.trantoc.entity.Gender;
import lombok.Builder;

import java.util.List;

@Builder
public record AncestorNode(
        Long id,
        String fullName,
        Gender gender,
        Integer birthYear,
        Integer deathYear,
        List<AncestorNode> parents
) {
}
