package com.ong.empuje.comunitario.web_services.service;

import com.ong.empuje.comunitario.web_services.model.Donation;
import java.time.LocalDateTime;
import java.util.List;

public interface DonationService {
    List<Donation> findByFilters(Integer categoryId, LocalDateTime startDate, LocalDateTime endDate, Boolean deleted);
}