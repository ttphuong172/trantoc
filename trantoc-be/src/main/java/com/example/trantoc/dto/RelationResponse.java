package com.example.trantoc.dto;

import com.example.trantoc.entity.RelationType;

public record RelationResponse(
        Long relatedPersonId,
        String relatedPersonName,
        RelationType relationType
) {
}