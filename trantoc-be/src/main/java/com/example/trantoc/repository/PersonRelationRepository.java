package com.example.trantoc.repository;

import com.example.trantoc.entity.Person;
import com.example.trantoc.entity.PersonRelation;
import com.example.trantoc.entity.RelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRelationRepository extends JpaRepository<PersonRelation, Long> {
    List<PersonRelation> findByPerson(Person person);

    List<PersonRelation> findByPersonAndRelationType (Person person, RelationType relationType);

    List<PersonRelation> findByPersonId(Long personId);

    List<PersonRelation> findByRelatedPersonId(Long relatedPersonId);

    List<PersonRelation> findByPersonIdAndRelationType(
            Long personId,
            RelationType relationType);

    boolean existsByPersonIdAndRelatedPersonIdAndRelationType(
            Long personId,
            Long relatedPersonId,
            RelationType relationType
    );

    boolean existsByPersonIdAndRelationType(
            Long personId,
            RelationType relationType
    );

    Optional<PersonRelation>
    findByPersonIdAndRelatedPersonIdAndRelationType(
            Long personId,
            Long relatedPersonId,
            RelationType relationType);

    boolean existsByPersonId(Long personId);

    boolean existsByRelatedPersonId(Long relatedPersonId);

    void deleteByPersonIdAndRelationType(
            Long personId,
            RelationType relationType);

    @Query("""
    select max(r.childOrder)
    from PersonRelation r
    where r.relatedPerson.id = :fatherId
      and r.relationType = 'FATHER'
    """)
    Integer findMaxChildOrderByFatherId(
            @Param("fatherId") Long fatherId);

    Optional<PersonRelation>
    findByRelatedPersonIdAndRelationTypeAndChildOrder(
            Long relatedPersonId,
            RelationType relationType,
            Integer childOrder
    );

    List<PersonRelation>
    findByRelatedPersonIdAndRelationType(
            Long relatedPersonId,
            RelationType relationType);



}
