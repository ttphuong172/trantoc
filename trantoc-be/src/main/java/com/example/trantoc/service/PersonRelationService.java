package com.example.trantoc.service;

import com.example.trantoc.dto.*;
import com.example.trantoc.entity.Gender;
import com.example.trantoc.entity.Person;
import com.example.trantoc.entity.PersonRelation;
import com.example.trantoc.entity.RelationType;
import com.example.trantoc.exception.BusinessException;
import com.example.trantoc.repository.PersonRelationRepository;
import com.example.trantoc.repository.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class PersonRelationService {

    private final PersonRepository personRepository;
    private final PersonRelationRepository personRelationRepository;

    public PersonRelationService(
            PersonRepository personRepository,
            PersonRelationRepository personRelationRepository) {
        this.personRepository = personRepository;
        this.personRelationRepository = personRelationRepository;
    }

    public List<RelationResponse> findByPersonId(Long personId) {

        return personRelationRepository.findByPersonId(personId)
                .stream()
                .map(relation -> new RelationResponse(
                        relation.getRelatedPerson().getId(),
                        relation.getRelatedPerson().getFullName(),
                        relation.getRelationType()
                ))
                .toList();
    }

    private void validateRelation(
            CreateRelationRequest request,
            Person relatedPerson) {

        validateSelfRelation(request);

        validateParentConstraint(request);

        validateGender(request, relatedPerson);

        validateDuplicateRelation(request);

        validateCycle(
                request,
                request.getPersonId(),
                relatedPerson.getId());
    }

    private void validateSelfRelation(
            CreateRelationRequest request) {

        if (request.getPersonId()
                .equals(request.getRelatedPersonId())) {

            throw new BusinessException(
                    "Person cannot relate to itself");
        }
    }

    private void validateParentConstraint(
            CreateRelationRequest request) {

        if (request.getRelationType() == RelationType.FATHER
                && personRelationRepository
                .existsByPersonIdAndRelationType(
                        request.getPersonId(),
                        RelationType.FATHER)) {

            throw new BusinessException(
                    "Father already exists");
        }

        if (request.getRelationType() == RelationType.MOTHER
                && personRelationRepository
                .existsByPersonIdAndRelationType(
                        request.getPersonId(),
                        RelationType.MOTHER)) {

            throw new BusinessException(
                    "Mother already exists");
        }
    }

    private void validateGender(
            CreateRelationRequest request,
            Person relatedPerson) {

        if (request.getRelationType() == RelationType.FATHER
                && relatedPerson.getGender() != Gender.MALE) {

            throw new BusinessException(
                    "Father must be MALE");
        }

        if (request.getRelationType() == RelationType.MOTHER
                && relatedPerson.getGender() != Gender.FEMALE) {

            throw new BusinessException(
                    "Mother must be FEMALE");
        }
    }

    private void validateDuplicateRelation(
            CreateRelationRequest request) {

        boolean exists =
                personRelationRepository
                        .existsByPersonIdAndRelatedPersonIdAndRelationType(
                                request.getPersonId(),
                                request.getRelatedPersonId(),
                                request.getRelationType());

        if (exists) {

            throw new BusinessException(
                    "Relation already exists");
        }
    }

    private void validateCycle(
            CreateRelationRequest request,
            Long personId,
            Long relatedPersonId) {

        if ((request.getRelationType() == RelationType.FATHER
                || request.getRelationType() == RelationType.MOTHER)
                && isAncestor(
                relatedPersonId,
                personId)) {

            throw new BusinessException(
                    "Cycle detected in family tree");
        }
    }

    private void createReverseSpouseRelation(
            Person person,
            Person relatedPerson) {

        boolean reverseExists =
                personRelationRepository
                        .existsByPersonIdAndRelatedPersonIdAndRelationType(
                                relatedPerson.getId(),
                                person.getId(),
                                RelationType.SPOUSE);

        if (!reverseExists) {

            PersonRelation reverseRelation =
                    new PersonRelation();

            reverseRelation.setPerson(relatedPerson);
            reverseRelation.setRelatedPerson(person);
            reverseRelation.setRelationType(RelationType.SPOUSE);

            personRelationRepository.save(reverseRelation);
        }
    }

    @Transactional
    public PersonRelation create(CreateRelationRequest request) {

        Person person = personRepository.findById(
                        request.getPersonId())
                .orElseThrow();

        Person relatedPerson = personRepository.findById(
                        request.getRelatedPersonId())
                .orElseThrow();

        validateRelation(
                request,
                relatedPerson);

        // SPOUSE
        if (request.getRelationType() == RelationType.SPOUSE) {

            relatedPerson.setGeneration(
                    person.getGeneration());

            personRepository.save(relatedPerson);
        }

        // FATHER
        if (request.getRelationType() == RelationType.FATHER) {

            if (relatedPerson.getGeneration() == null) {
                throw new IllegalStateException(
                        "Người cha chưa có đời");
            }

            person.setGeneration(
                    relatedPerson.getGeneration() + 1);

            personRepository.save(person);
        }

        PersonRelation relation = new PersonRelation();

        relation.setPerson(person);
        relation.setRelatedPerson(relatedPerson);
        relation.setRelationType(request.getRelationType());

        if (request.getRelationType() == RelationType.FATHER) {

            Integer maxOrder =
                    personRelationRepository
                            .findMaxChildOrderByFatherId(
                                    relatedPerson.getId());

            relation.setChildOrder(
                    maxOrder == null
                            ? 1
                            : maxOrder + 1
            );
        }

        PersonRelation savedRelation =
                personRelationRepository.save(relation);

        if (request.getRelationType() == RelationType.SPOUSE) {

            createReverseSpouseRelation(
                    person,
                    relatedPerson);
        }

        return savedRelation;
    }

    private AncestorNode buildAncestorTree(Person person) {

        List<PersonRelation> relations =
                personRelationRepository.findByPersonId(person.getId());

        List<AncestorNode> parents = relations.stream()
                .filter(r ->
                        r.getRelationType() == RelationType.FATHER
                                || r.getRelationType() == RelationType.MOTHER)
                .map(r -> buildAncestorTree(r.getRelatedPerson()))
                .toList();

        return AncestorNode.builder()
                .id(person.getId())
                .fullName(person.getFullName())
                .gender(person.getGender())
                .birthYear(person.getBirthYear())
                .deathYear(person.getDeathYear())
                .parents(parents)
                .build();

    }

    public AncestorNode getAncestorTree(Long personId) {

        Person person = personRepository.findById(personId)
                .orElseThrow();

        return buildAncestorTree(person);
    }

    public List<ChildResponse> getChildren(Long parentId) {

        return personRelationRepository
                .findByRelatedPersonId(parentId)
                .stream()
                .filter(r ->
                        r.getRelationType() == RelationType.FATHER || r.getRelationType() == RelationType.MOTHER)
                .sorted(
                        Comparator.comparing(
                                PersonRelation::getChildOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        )
                )
                .map(r -> {

                    String motherName =
                            personRelationRepository
                                    .findByPersonIdAndRelationType(
                                            r.getPerson().getId(),
                                            RelationType.MOTHER
                                    )
                                    .stream()
                                    .findFirst()
                                    .map(m -> m.getRelatedPerson().getFullName())
                                    .orElse(null);

                    return new ChildResponse(
                            r.getPerson().getId(),
                            r.getPerson().getFullName(),
                            r.getPerson().getAliasName(),
                            r.getPerson().getGender(),
                            r.getChildOrder(),
                            motherName
                    );

                })
                .toList();
    }


    public List<PersonSummaryResponse> getSpouses(Long personId) {

        return personRelationRepository
                .findByPersonIdAndRelationType(
                        personId,
                        RelationType.SPOUSE)
                .stream()
                .map(r -> new PersonSummaryResponse(
                        r.getRelatedPerson().getId(),
                        r.getRelatedPerson().getFullName(),
                        r.getRelatedPerson().getAliasName()
                ))
                .toList();
    }

    public ParentResponse getParents(Long personId) {

        List<PersonRelation> relations =
                personRelationRepository.findByPersonId(personId);

        PersonSummaryResponse father = null;
        PersonSummaryResponse mother = null;

        for (PersonRelation relation : relations) {

            if (relation.getRelationType() == RelationType.FATHER) {

                Person person = relation.getRelatedPerson();

                father = new PersonSummaryResponse(
                        person.getId(),
                        person.getFullName(),
                        person.getAliasName()
                );
            }

            if (relation.getRelationType() == RelationType.MOTHER) {

                Person person = relation.getRelatedPerson();

                mother = new PersonSummaryResponse(
                        person.getId(),
                        person.getFullName(),
                        person.getAliasName()
                );
            }
        }

        return new ParentResponse(
                father,
                mother
        );
    }

    private boolean isAncestor(Long personId,
                               Long ancestorId) {

        List<PersonRelation> relations =
                personRelationRepository.findByPersonId(personId);

        for (PersonRelation relation : relations) {

            if (relation.getRelationType() == RelationType.FATHER
                    || relation.getRelationType() == RelationType.MOTHER) {

                Long parentId =
                        relation.getRelatedPerson().getId();

                if (parentId.equals(ancestorId)) {
                    return true;
                }

                if (isAncestor(parentId, ancestorId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void delete(Long relationId) {

        PersonRelation relation =
                personRelationRepository.findById(
                                relationId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Relation not found"));

        personRelationRepository.delete(relation);

        if (relation.getRelationType() == RelationType.SPOUSE) {

            personRelationRepository
                    .findByPersonIdAndRelatedPersonIdAndRelationType(
                            relation.getRelatedPerson().getId(),
                            relation.getPerson().getId(),
                            RelationType.SPOUSE)
                    .ifPresent(
                            personRelationRepository::delete
                    );
        }
    }

    private Integer calculateGeneration(
            Long personId,
            Long rootId) {

        if (personId.equals(rootId)) {
            return 1;
        }

        List<PersonRelation> relations =
                personRelationRepository.findByPersonId(personId);

        for (PersonRelation relation : relations) {

            if (relation.getRelationType() == RelationType.FATHER
                    || relation.getRelationType() == RelationType.MOTHER) {

                Integer parentGeneration =
                        calculateGeneration(
                                relation.getRelatedPerson().getId(),
                                rootId);

                if (parentGeneration != null) {
                    return parentGeneration + 1;
                }
            }
        }

        return null;
    }

    public List<PersonSummaryResponse>
    getCandidateMothers(Long personId) {

        ParentResponse parents =
                getParents(personId);

        if (parents.father() == null) {
            return List.of();
        }

        return getSpouses(
                parents.father().id());
    }

    public void changeMother(
            Long personId,
            Long motherId) {

        Person person =
                personRepository.findById(personId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Person not found"));

        Person mother =
                personRepository.findById(motherId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Mother not found"));

        if (mother.getGender() != Gender.FEMALE) {

            throw new BusinessException(
                    "Mother must be FEMALE");
        }

        personRelationRepository
                .deleteByPersonIdAndRelationType(
                        personId,
                        RelationType.MOTHER);

        PersonRelation relation =
                new PersonRelation();

        relation.setPerson(person);
        relation.setRelatedPerson(mother);
        relation.setRelationType(
                RelationType.MOTHER);

        personRelationRepository.save(
                relation);
    }

    private DescendantNode buildDescendantTree(
            Person person) {

        List<DescendantNode> children =
                getChildren(person.getId())
                        .stream()
                        .map(child -> {

                            Person childPerson =
                                    personRepository
                                            .findById(child.id())
                                            .orElseThrow();

                            return buildDescendantTree(
                                    childPerson);

                        })
                        .toList();

        return DescendantNode.builder()
                .id(person.getId())
                .fullName(person.getFullName())
                .aliasName(person.getAliasName())
                .children(children)
                .build();
    }

    public DescendantNode getDescendantTree(
            Long personId) {

        Person person =
                personRepository.findById(personId)
                        .orElseThrow();

        return buildDescendantTree(person);
    }

    @Transactional
    public void rebuildGenerations() {

        Person root = personRepository
                .findByRootAncestorTrue()
                .orElseThrow();

        List<Person> persons =
                personRepository.findAll();

        for (Person person : persons) {

            Integer generation =
                    calculateGeneration(
                            person.getId(),
                            root.getId());

            person.setGeneration(
                    generation);

        }

        personRepository.saveAll(persons);
    }

    @Transactional
    public void moveUp(
            Long fatherId,
            Long childId) {

        PersonRelation current =
                personRelationRepository
                        .findByPersonIdAndRelatedPersonIdAndRelationType(
                                childId,
                                fatherId,
                                RelationType.FATHER
                        )
                        .orElseThrow();

        if (current.getChildOrder() == null ||
                current.getChildOrder() <= 1) {
            return;
        }

        PersonRelation previous =
                personRelationRepository
                        .findByRelatedPersonIdAndRelationTypeAndChildOrder(
                                fatherId,
                                RelationType.FATHER,
                                current.getChildOrder() - 1
                        )
                        .orElse(null);

        if (previous == null) {
            return;
        }

        Integer temp =
                current.getChildOrder();

        current.setChildOrder(
                previous.getChildOrder());

        previous.setChildOrder(temp);

        personRelationRepository.save(current);
        personRelationRepository.save(previous);
    }

    @Transactional
    public void moveDown(
            Long fatherId,
            Long childId) {

        PersonRelation current =
                personRelationRepository
                        .findByPersonIdAndRelatedPersonIdAndRelationType(
                                childId,
                                fatherId,
                                RelationType.FATHER)
                        .orElseThrow();

        Integer currentOrder =
                current.getChildOrder();

        if (currentOrder == null) {
            return;
        }

        PersonRelation next =
                personRelationRepository
                        .findByRelatedPersonIdAndRelationTypeAndChildOrder(
                                fatherId,
                                RelationType.FATHER,
                                currentOrder + 1)
                        .orElse(null);

        if (next == null) {
            return;
        }

        current.setChildOrder(
                next.getChildOrder());

        next.setChildOrder(
                currentOrder);

        personRelationRepository.save(current);
        personRelationRepository.save(next);
    }

    public List<Integer> buildPath(Long personId) {

        List<PersonRelation> fatherRelations =
                personRelationRepository
                        .findByPersonIdAndRelationType(
                                personId,
                                RelationType.FATHER);

        // Không có cha trong dòng họ
        if (fatherRelations.isEmpty()) {

            List<PersonRelation> spouses =
                    personRelationRepository
                            .findByPersonIdAndRelationType(
                                    personId,
                                    RelationType.SPOUSE);

            if (!spouses.isEmpty()) {

                Long spouseId =
                        spouses.getFirst()
                                .getRelatedPerson()
                                .getId();

                List<PersonRelation> spouseFatherRelations =
                        personRelationRepository
                                .findByPersonIdAndRelationType(
                                        spouseId,
                                        RelationType.FATHER);

                // Chỉ lấy path của vợ/chồng khi
                // vợ/chồng có cha trong dòng họ
                if (!spouseFatherRelations.isEmpty()) {
                    return buildPath(spouseId);
                }
            }

            return List.of();
        }

        List<Integer> path = new ArrayList<>();

        Long currentPersonId = personId;

        while (true) {

            List<PersonRelation> relations =
                    personRelationRepository
                            .findByPersonIdAndRelationType(
                                    currentPersonId,
                                    RelationType.FATHER);

            if (relations.isEmpty()) {
                break;
            }

            PersonRelation fatherRelation =
                    relations.getFirst();

            path.add(
                    fatherRelation.getChildOrder());

            currentPersonId =
                    fatherRelation
                            .getRelatedPerson()
                            .getId();
        }

        Collections.reverse(path);

        return path;
    }

    public int comparePath(
            List<Integer> a,
            List<Integer> b) {

        int size = Math.min(
                a.size(),
                b.size());

        for (int i = 0; i < size; i++) {

            int cmp =
                    Integer.compare(
                            a.get(i),
                            b.get(i));

            if (cmp != 0) {
                return cmp;
            }
        }

        return Integer.compare(
                a.size(),
                b.size());
    }

}