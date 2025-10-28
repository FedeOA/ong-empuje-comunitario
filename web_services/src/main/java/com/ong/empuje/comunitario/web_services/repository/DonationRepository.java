package com.ong.empuje.comunitario.web_services.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ong.empuje.comunitario.web_services.model.Donation;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Integer> {
    @Query("SELECT d FROM Donation d WHERE " +
        "(:categoryId IS NULL OR d.category.id = :categoryId) " +
        "AND (:startDate IS NULL OR d.createdAt >= :startDate) " +
        "AND (:endDate IS NULL OR d.createdAt <= :endDate) " +
        "AND (:deleted IS NULL OR d.deleted = :deleted)")
    List<Donation> findByFilters(@Param("categoryId") Integer categoryId,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                @Param("deleted") Boolean deleted);
}