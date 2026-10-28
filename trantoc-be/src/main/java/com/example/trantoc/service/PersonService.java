package com.example.trantoc.service;

import com.example.trantoc.dto.PersonDetailResponse;
import com.example.trantoc.dto.PersonListResponse;
import com.example.trantoc.entity.*;
import com.example.trantoc.exception.BusinessException;
import com.example.trantoc.repository.PersonRelationRepository;
import com.example.trantoc.repository.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    private final PersonRelationRepository personRelationRepository;

    private final PersonRelationService personRelationService;


    public PersonService(
            PersonRepository personRepository,
            PersonRelationRepository personRelationRepository,
            PersonRelationService personRelationService) {

        this.personRepository = personRepository;
        this.personRelationRepository = personRelationRepository;
        this.personRelationService = personRelationService;
    }

    public Page<Person> findAll(Pageable pageable) {

        return personRepository.findAll(pageable);
    }

    public Person findById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Person not found"));
    }

    public PersonDetailResponse getDetail(
            Long id) {

        Person person =
                personRepository.findById(id)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Person not found"));

        return new PersonDetailResponse(
                person.getId(),
                person.getFullName(),
                person.getAliasName(),
                person.getGeneration(),
                person.getBirthYear(),
                person.getDeathYear(),
                person.getNotes(),
                person.getGender(),
                person.getAddress(),
                person.getBurialPlace(),
                person.getDeathAnniversary(),
                person.getRootAncestor(),
                determineRole(person)
        );
    }

    public Person save(Person person) {
        return personRepository.save(person);
    }

    public Person update(
            Long id,
            Person request) {

        Person person = personRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Person not found"));

        person.setFullName(request.getFullName());
        person.setAliasName(request.getAliasName());

        person.setBirthYear(request.getBirthYear());
        person.setDeathYear(request.getDeathYear());

        person.setGender(request.getGender());

        person.setAddress(request.getAddress());

        person.setBurialPlace(
                request.getBurialPlace());

        person.setDeathAnniversary(
                request.getDeathAnniversary());

        person.setRootAncestor(
                request.getRootAncestor());

        person.setNotes(
                request.getNotes());

        return personRepository.save(person);
    }

    public Page<PersonListResponse> search(
            String keyword,
            Integer generation,
            Pageable pageable) {

        List<Person> persons =
                new ArrayList<>(
                        personRepository.search(
                                        keyword,
                                        generation,
                                        Pageable.unpaged())
                                .getContent()
                );

        Map<Long, List<Integer>> pathMap =
                new HashMap<>();

        for (Person person : persons) {

            pathMap.put(
                    person.getId(),
                    personRelationService.buildPath(
                            person.getId()));
        }

        persons.sort((p1, p2) -> {

            int result =
                    personRelationService.comparePath(
                            pathMap.get(p1.getId()),
                            pathMap.get(p2.getId()));

            if (result != 0) {
                return result;
            }

            FamilyRole role1 =
                    determineRole(p1);

            FamilyRole role2 =
                    determineRole(p2);

            boolean spouse1 =
                    role1 == FamilyRole.DAUGHTER_IN_LAW
                            || role1 == FamilyRole.SON_IN_LAW;

            boolean spouse2 =
                    role2 == FamilyRole.DAUGHTER_IN_LAW
                            || role2 == FamilyRole.SON_IN_LAW;

            if (spouse1 != spouse2) {
                return spouse1 ? 1 : -1;
            }

            return p1.getId()
                    .compareTo(p2.getId());
        });

        List<PersonListResponse> responses =
                persons.stream()
                        .map(person -> {

                            Person father =
                                    personRelationRepository
                                            .findByPersonIdAndRelationType(
                                                    person.getId(),
                                                    RelationType.FATHER
                                            )
                                            .stream()
                                            .findFirst()
                                            .map(PersonRelation::getRelatedPerson)
                                            .orElse(null);

                            String fatherName =
                                    father != null
                                            ? father.getFullName()
                                            : null;

                            String fatherAliasName =
                                    father != null
                                            ? father.getAliasName()
                                            : null;

                            Person mother =
                                    personRelationRepository
                                            .findByPersonIdAndRelationType(
                                                    person.getId(),
                                                    RelationType.MOTHER
                                            )
                                            .stream()
                                            .findFirst()
                                            .map(PersonRelation::getRelatedPerson)
                                            .orElse(null);

                            String motherName =
                                    mother != null
                                            ? mother.getFullName()
                                            : null;

                            String motherAliasName =
                                    mother != null
                                            ? mother.getAliasName()
                                            : null;

                            Person spouse =
                                    personRelationRepository
                                            .findByPersonIdAndRelationType(
                                                    person.getId(),
                                                    RelationType.SPOUSE
                                            )
                                            .stream()
                                            .findFirst()
                                            .map(PersonRelation::getRelatedPerson)
                                            .orElse(null);

                            String spouseName =
                                    spouse != null
                                            ? spouse.getFullName()
                                            : null;

                            String spouseAliasName =
                                    spouse != null
                                            ? spouse.getAliasName()
                                            : null;

                            FamilyRole role =
                                    determineRole(person);

                            List<String> children =
                                    personRelationRepository
                                            .findByRelatedPersonIdAndRelationType(
                                                    person.getId(),
                                                    RelationType.FATHER)
                                            .stream()
                                            .sorted(
                                                    Comparator.comparing(
                                                            PersonRelation::getChildOrder))
                                            .map(PersonRelation::getPerson)
                                            .map(child -> {

                                                if (child.getAliasName() == null
                                                        || child.getAliasName().isBlank()) {
                                                    return child.getFullName();
                                                }

                                                return child.getFullName()
                                                        + " tức "
                                                        + child.getAliasName();
                                            })
                                            .toList();

                            return new PersonListResponse(
                                    person.getId(),
                                    person.getFullName(),
                                    person.getAliasName(),
                                    person.getGeneration(),
                                    fatherName,
                                    fatherAliasName,
                                    motherName,
                                    motherAliasName,
                                    spouseName,
                                    spouseAliasName,
                                    role,
                                    children
                            );
                        })
                        .toList();

        int start = (int) pageable.getOffset();

        int end = Math.min(
                start + pageable.getPageSize(),
                responses.size());

        List<PersonListResponse> pageContent =
                responses.subList(start, end);

        return new PageImpl<>(
                pageContent,
                pageable,
                responses.size());
    }

//    public Page<PersonListResponse> search(
//            String keyword,
//            Integer generation,
//            Pageable pageable) {
//
//        return personRepository.search(
//                        keyword,
//                        generation,
//                        pageable)
//                .map(person -> {
//
//                    Person father =
//                            personRelationRepository
//                                    .findByPersonIdAndRelationType(
//                                            person.getId(),
//                                            RelationType.FATHER
//                                    )
//                                    .stream()
//                                    .findFirst()
//                                    .map(PersonRelation::getRelatedPerson)
//                                    .orElse(null);
//
//                    String fatherName =
//                            father != null
//                                    ? father.getFullName()
//                                    : null;
//
//                    String fatherAliasName =
//                            father != null
//                                    ? father.getAliasName()
//                                    : null;
//
//                    Person mother =
//                            personRelationRepository
//                                    .findByPersonIdAndRelationType(
//                                            person.getId(),
//                                            RelationType.MOTHER
//                                    )
//                                    .stream()
//                                    .findFirst()
//                                    .map(PersonRelation::getRelatedPerson)
//                                    .orElse(null);
//
//                    String motherName =
//                            mother != null
//                                    ? mother.getFullName()
//                                    : null;
//
//                    String motherAliasName =
//                            mother != null
//                                    ? mother.getAliasName()
//                                    : null;
//
//                    Person spouse =
//                            personRelationRepository
//                                    .findByPersonIdAndRelationType(
//                                            person.getId(),
//                                            RelationType.SPOUSE
//                                    )
//                                    .stream()
//                                    .findFirst()
//                                    .map(PersonRelation::getRelatedPerson)
//                                    .orElse(null);
//
//                    String spouseName =
//                            spouse != null
//                                    ? spouse.getFullName()
//                                    : null;
//
//                    String spouseAliasName =
//                            spouse != null
//                                    ? spouse.getAliasName()
//                                    : null;
//
//                    FamilyRole role =
//                            determineRole(person);
//
//                    return new PersonListResponse(
//                            person.getId(),
//                            person.getFullName(),
//                            person.getAliasName(),
//                            person.getGeneration(),
//                            fatherName,
//                            fatherAliasName,
//                            motherName,
//                            motherAliasName,
//                            spouseName,
//                            spouseAliasName,
//                            role
//                    );
//                });
//    }

    public void delete(Long id) {

        Person person = personRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Person not found"));

        boolean hasSpouse =
                !personRelationRepository
                        .findByPersonIdAndRelationType(
                                id,
                                RelationType.SPOUSE)
                        .isEmpty();

        if (hasSpouse) {

            throw new BusinessException(
                    "Cannot delete person with spouse");
        }

        personRelationRepository.deleteAll(
                personRelationRepository.findByPersonId(id)
        );

        personRepository.delete(person);
    }

    public FamilyRole determineRole(Person person) {

        if (Boolean.TRUE.equals(
                person.getRootAncestor())) {

            return FamilyRole.SON;
        }

        boolean hasParent =
                !personRelationRepository
                        .findByPersonIdAndRelationType(
                                person.getId(),
                                RelationType.FATHER)
                        .isEmpty()
                        ||
                        !personRelationRepository
                                .findByPersonIdAndRelationType(
                                        person.getId(),
                                        RelationType.MOTHER)
                                .isEmpty();

        boolean hasSpouse =
                !personRelationRepository
                        .findByPersonIdAndRelationType(
                                person.getId(),
                                RelationType.SPOUSE)
                        .isEmpty();

        if (hasParent) {

            return person.getGender() == Gender.MALE
                    ? FamilyRole.SON
                    : FamilyRole.DAUGHTER;
        }

        if (hasSpouse) {

            return person.getGender() == Gender.MALE
                    ? FamilyRole.SON_IN_LAW
                    : FamilyRole.DAUGHTER_IN_LAW;
        }

        return person.getGender() == Gender.MALE
                ? FamilyRole.SON
                : FamilyRole.DAUGHTER;
    }

    public List<Integer> getGenerations() {

        return personRepository.findAllGenerations();
    }








}