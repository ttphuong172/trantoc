package com.example.trantoc.repository;

import com.example.trantoc.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByFullNameContainingIgnoreCase(
            String keyword);

    Page<Person> findByFullNameContainingIgnoreCase(
            String keyword,
            Pageable pageable);

    Optional<Person> findByRootAncestorTrue();

    @Query("""
    select p
    from Person p
    where (:generation is null
           or p.generation = :generation)
      and (
           :keyword is null
           or trim(:keyword) = ''
           or lower(p.fullName)
                like lower(concat('%', :keyword, '%'))
           or lower(coalesce(p.aliasName, ''))
                like lower(concat('%', :keyword, '%'))
      )
    """)
    Page<Person> search(
            @Param("keyword") String keyword,
            @Param("generation") Integer generation,
            Pageable pageable);

    @Query("""
    select distinct p.generation
    from Person p
    where p.generation is not null
    order by p.generation
    """)
    List<Integer> findAllGenerations();

}
