package com.example.trantoc.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DescendantNode(
        Long id,
        String fullName,
        String aliasName,
        List<DescendantNode> children
) {
}