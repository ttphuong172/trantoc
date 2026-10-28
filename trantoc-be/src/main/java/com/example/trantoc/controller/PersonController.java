package com.example.trantoc.controller;

import com.example.trantoc.dto.*;
import com.example.trantoc.entity.Person;
import com.example.trantoc.entity.PersonRelation;
import com.example.trantoc.service.PersonRelationService;
import com.example.trantoc.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;
    private final PersonRelationService personRelationService;

    public PersonController(
            PersonService personService,
            PersonRelationService personRelationService) {

        this.personService = personService;
        this.personRelationService = personRelationService;
    }

    @GetMapping("/{id}")
    public PersonDetailResponse getPerson(
            @PathVariable Long id) {

        return personService.getDetail(id);
    }

    @PostMapping
    public Person createPerson(@RequestBody Person person) {
        return personService.save(person);
    }

    @GetMapping("/{id}/relations")
    public List<RelationResponse> getRelations(
            @PathVariable Long id) {

        return personRelationService.findByPersonId(id);
    }

    @GetMapping("/{id}/parents")
    public ParentResponse getParents(@PathVariable Long id) {
        return personRelationService.getParents(id);
    }

    @GetMapping("/{id}/ancestors")
    public AncestorNode getAncestorTree(
            @PathVariable Long id) {

        return personRelationService.getAncestorTree(id);
    }

    @GetMapping("/{id}/descendants")
    public DescendantNode getDescendantTree(
            @PathVariable Long id) {

        return personRelationService
                .getDescendantTree(id);
    }

    @GetMapping("/{id}/children")
    public List<ChildResponse> getChildren(
            @PathVariable Long id) {

        return personRelationService.getChildren(id);
    }

    @GetMapping("/{id}/spouses")
    public List<PersonSummaryResponse> getSpouses(
            @PathVariable Long id) {

        return personRelationService.getSpouses(id);
    }

    @PutMapping("/{id}")
    public Person updatePerson(
            @PathVariable Long id,
            @RequestBody Person request) {

        return personService.update(
                id,
                request);
    }

    @GetMapping
    public Page<PersonListResponse> getPersons(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            Integer generation,

            Pageable pageable) {

        Pageable sortedPageable =
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(
                                Sort.Order.asc("generation"),
                                Sort.Order.asc("fullName")
                        )
                );

        return personService.search(
                keyword,
                generation,
                sortedPageable);
    }

    @DeleteMapping("/{id}")
    public void deletePerson(
            @PathVariable Long id) {

        personService.delete(id);
    }

    @GetMapping(
            "/{personId}/candidate-mothers")
    public List<PersonSummaryResponse>
    getCandidateMothers(
            @PathVariable Long personId) {

        return personRelationService
                .getCandidateMothers(personId);
    }

    @GetMapping("/generations")
    public List<Integer> getGenerations() {

        return personService.getGenerations();

    }

    @PostMapping("/rebuild-generations")
    public void rebuildGenerations() {

        personRelationService
                .rebuildGenerations();

    }

    @GetMapping("/{id}/path")
    public List<Integer> getPath(
            @PathVariable Long id) {

        return personRelationService.buildPath(id);
    }

    @DeleteMapping("/{personId}/spouses/{spouseId}")
    public void deleteSpouse(
            @PathVariable Long personId,
            @PathVariable Long spouseId) {

        personRelationService.deleteSpouse(
                personId,
                spouseId);
    }


}