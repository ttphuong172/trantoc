package com.example.trantoc.dto;

import com.example.trantoc.entity.RelationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRelationRequest {

    private Long personId;

    private Long relatedPersonId;

    private RelationType relationType;
}