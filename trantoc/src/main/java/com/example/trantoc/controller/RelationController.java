package com.example.trantoc.controller;

import com.example.trantoc.dto.CreateRelationRequest;
import com.example.trantoc.entity.PersonRelation;
import com.example.trantoc.service.PersonRelationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relations")
public class RelationController {

    private final PersonRelationService personRelationService;

    public RelationController(PersonRelationService personRelationService) {
        this.personRelationService = personRelationService;
    }

    @PostMapping
    public PersonRelation createRelation(
            @RequestBody CreateRelationRequest request) {

        return personRelationService.create(request);
    }

    @DeleteMapping("/{id}")
    public void deleteRelation(
            @PathVariable Long id) {

        personRelationService.delete(id);
    }

    @PutMapping(
            "/persons/{personId}/mother/{motherId}")
    public void changeMother(
            @PathVariable Long personId,
            @PathVariable Long motherId) {

        personRelationService.changeMother(
                personId,
                motherId);
    }

    @PostMapping(
            "/father/{fatherId}/children/{childId}/move-up"
    )
    public void moveUp(
            @PathVariable Long fatherId,
            @PathVariable Long childId) {

        personRelationService.moveUp(
                fatherId,
                childId);
    }

    @PostMapping(
            "/father/{fatherId}/children/{childId}/move-down"
    )
    public void moveDown(
            @PathVariable Long fatherId,
            @PathVariable Long childId) {

        personRelationService.moveDown(
                fatherId,
                childId);
    }

}