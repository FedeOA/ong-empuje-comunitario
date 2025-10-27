package com.ong.empuje.comunitario.web_services.service.impl;

import com.ong.empuje.comunitario.web_services.model.Donation;
import com.ong.empuje.comunitario.web_services.repository.DonationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.ong.empuje.comunitario.web_services.service.DonationService;

@Service
public class DonationServiceImpl implements DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Override
    public List<Donation> findByFilters(Integer categoryId, LocalDateTime startDate, LocalDateTime endDate, Boolean deleted) {
        return donationRepository.findByFilters(categoryId, startDate, endDate, deleted);
    }
}