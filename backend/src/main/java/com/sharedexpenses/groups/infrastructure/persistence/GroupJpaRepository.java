package com.sharedexpenses.groups.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface GroupJpaRepository extends JpaRepository<GroupEntity, Long> {

    @Query("select distinct g from GroupEntity g join g.members m where m.userId = :userId order by g.createdAt desc")
    List<GroupEntity> findByMember(@Param("userId") Long userId);
}
