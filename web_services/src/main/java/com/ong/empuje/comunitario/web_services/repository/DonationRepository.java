package com.ong.empuje.comunitario.web_services.repository;

import com.ong.empuje.comunitario.web_services.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    @Query("SELECT d FROM Donation d WHERE " +
           "(:categoryId IS NULL OR d.categoryId = :categoryId) AND " +
           "(:startDate IS NULL OR d.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR d.createdAt <= :endDate) AND " +
           "(:deleted IS NULL OR d.deleted = :deleted)")
    List<Donation> findByFilters(
        @Param("categoryId") Integer categoryId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("deleted") Boolean deleted
    );
}