package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.DonationReportGroup;
import com.ong.empuje.comunitario.web_services.dto.FilterInput;
import com.ong.empuje.comunitario.web_services.model.*;
import com.ong.empuje.comunitario.web_services.repository.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DonationReportController {
    private final DonationRepository donationRepository;
    private final SavedFilterRepository savedFilterRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public DonationReportController(
            DonationRepository donationRepository, 
            SavedFilterRepository savedFilterRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.donationRepository = donationRepository;
        this.savedFilterRepository = savedFilterRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @QueryMapping
    public List<DonationReportGroup> donationReport(
        @Argument Integer categoryId,
        @Argument String startDate,
        @Argument String endDate,
        @Argument String deleted
    ) {
        Boolean deletedFilter = null;
        if (deleted != null) {
            deletedFilter = deleted.equals("YES") ? true : deleted.equals("NO") ? false : null;
        }

        LocalDateTime startDateTime = null;
        if (startDate != null && !startDate.isEmpty()) {
            startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        LocalDateTime endDateTime = null;
        if (endDate != null && !endDate.isEmpty()) {
            endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        List<Donation> donations = donationRepository.findByFilters(
            categoryId,
            startDateTime,
            endDateTime,
            deletedFilter
        );

        Map<Integer, Map<Boolean, List<Donation>>> grouped = donations.stream()
            .collect(Collectors.groupingBy(
                donation -> donation.getCategoryId(),
                Collectors.groupingBy(Donation::getDeleted)
            ));

        List<DonationReportGroup> result = new ArrayList<>();
        for (Integer catId : grouped.keySet()) {
            Category category = categoryRepository.findById(catId).orElse(null);
            String categoryName = category != null ? category.getName() : "Desconocida";
            
            for (Boolean del : grouped.get(catId).keySet()) {
                List<Donation> groupDonations = grouped.get(catId).get(del);
                DonationReportGroup group = new DonationReportGroup();
                group.setCategoryId(catId);
                group.setCategoryName(categoryName);
                group.setDeleted(del);
                group.setTotalQuantity(groupDonations.stream().mapToInt(Donation::getQuantity).sum());
                group.setDonations(groupDonations);
                result.add(group);
            }
        }

        return result;
    }

    @QueryMapping
    public List<SavedFilter> savedFilters(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return savedFilterRepository.findByUserId(user.getId());
    }

    @QueryMapping
    public SavedFilter savedFilter(@Argument Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        SavedFilter filter = savedFilterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Filter not found"));
        if (!filter.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to filter");
        }
        return filter;
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    @QueryMapping
    public Category category(@Argument Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @MutationMapping
    public SavedFilter saveFilter(@Argument FilterInput input, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        SavedFilter filter = new SavedFilter();
        filter.setName(input.getName());
        
        // Set category if provided
        if (input.getCategoryId() != null) {
            Category category = categoryRepository.findById(input.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            filter.setCategory(category);
        }
        
        filter.setUser(user);
        filter.setStartDate(input.getStartDate());
        filter.setEndDate(input.getEndDate());
        filter.setDeleted(input.getDeletedStatus());
        
        return savedFilterRepository.save(filter);
    }

    @MutationMapping
    public SavedFilter updateFilter(@Argument Long id, @Argument FilterInput input, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        SavedFilter filter = savedFilterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Filter not found"));
        
        if (!filter.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to filter");
        }

        filter.setName(input.getName());
        
        // Update category if provided
        if (input.getCategoryId() != null) {
            Category category = categoryRepository.findById(input.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            filter.setCategory(category);
        } else {
            filter.setCategory(null);
        }
        
        filter.setStartDate(input.getStartDate());
        filter.setEndDate(input.getEndDate());
        filter.setDeleted(input.getDeletedStatus());
        
        return savedFilterRepository.save(filter);
    }

    @MutationMapping
    public Boolean deleteFilter(@Argument Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        SavedFilter filter = savedFilterRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Filter not found"));
        
        if (!filter.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to filter");
        }

        savedFilterRepository.deleteById(id);
        return true;
    }
}