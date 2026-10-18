package com.sharedexpenses.balances.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByGroupIdOrderByDateAscIdAsc(Long groupId);

    @Modifying
    @Query("delete from PaymentEntity p where p.groupId = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);
}
