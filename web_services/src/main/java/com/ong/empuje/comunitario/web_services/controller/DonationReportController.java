package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.in.DonationReportGroupDTO;
import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.Donation;
import com.ong.empuje.comunitario.web_services.model.SavedFilter;
import com.ong.empuje.comunitario.web_services.service.CategoryService;
import com.ong.empuje.comunitario.web_services.service.DonationService;
import com.ong.empuje.comunitario.web_services.service.SavedFilterService;
import com.ong.empuje.comunitario.web_services.service.UserService;

import graphql.GraphQLException;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ong.empuje.comunitario.web_services.dto.in.FilterInputDTO;
import com.ong.empuje.comunitario.web_services.model.User;

@Controller
public class DonationReportController {
    private static final Logger logger = LoggerFactory.getLogger(DonationReportController.class);

    private final DonationService donationService;
    private final CategoryService categoryService;
    private final SavedFilterService savedFilterService;
    private final UserService userService;

    public DonationReportController(
            DonationService donationService,
            CategoryService categoryService,
            SavedFilterService savedFilterService,
            UserService userService) {
        this.donationService = donationService;
        this.categoryService = categoryService;
        this.savedFilterService = savedFilterService;
        this.userService = userService;
    }

    @QueryMapping
    public List<DonationReportGroupDTO> donationReport(
            @Argument Integer categoryId,
            @Argument String startDate,
            @Argument String endDate,
            @Argument Boolean deleted
    ) {
        logger.info("Fetching donation report with filters: categoryId={}, startDate={}, endDate={}, deleted={}",
                categoryId, startDate, endDate, deleted);

        try {
            LocalDateTime startDateTime = null;
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    logger.error("Invalid startDate format: {}", startDate, e);
                    throw new GraphQLException("Invalid startDate format: " + startDate);
                }
            }

            LocalDateTime endDateTime = null;
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    logger.error("Invalid endDate format: {}", endDate, e);
                    throw new GraphQLException("Invalid endDate format: " + endDate);
                }
            }

            if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
                logger.error("End date {} is before start date {}", endDate, startDate);
                throw new GraphQLException("End date cannot be before start date");
            }

            List<Donation> donations = donationService.findByFilters(categoryId, startDateTime, endDateTime, deleted);
            logger.debug("Retrieved {} donations", donations.size());

            Map<Integer, Map<Boolean, List<Donation>>> grouped = donations.stream()
                    .collect(Collectors.groupingBy(
                            donation -> donation.getCategoryId() != null ? donation.getCategoryId() : 0,
                            Collectors.groupingBy(Donation::getDeleted)
                    ));

            List<DonationReportGroupDTO> result = new ArrayList<>();
            for (Integer catId : grouped.keySet()) {
                Category category = catId != 0 ? categoryService.findById(catId).orElse(null) : null;
                String categoryName = category != null ? category.getName() : "Desconocida";

                for (Boolean del : grouped.get(catId).keySet()) {
                    List<Donation> groupDonations = grouped.get(catId).get(del);
                    DonationReportGroupDTO group = new DonationReportGroupDTO();
                    group.setCategoryId(catId);
                    group.setCategoryName(categoryName);
                    group.setDeleted(del);
                    group.setTotalQuantity(groupDonations.stream()
                            .filter(d -> d.getQuantity() != null)
                            .mapToInt(Donation::getQuantity)
                            .sum());
                    group.setDonations(groupDonations);
                    result.add(group);
                }
            }

            logger.info("Returning {} donation report groups", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error processing donation report: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to process donation report: " + e.getMessage());
        }
    }

    @QueryMapping
    public List<Category> categories() {
        logger.info("Fetching all categories");
        try {
            List<Category> categories = categoryService.findAll();
            logger.info("Retrieved {} categories", categories.size());
            return categories;
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to fetch categories: " + e.getMessage());
        }
    }

    @QueryMapping
    public Category category(@Argument Integer id) {
        logger.info("Fetching category with id: {}", id);
        try {
            Category category = categoryService.findById(id).orElse(null);
            if (category == null) {
                logger.warn("Category not found for id: {}", id);
            }
            return category;
        } catch (Exception e) {
            logger.error("Error fetching category with id {}: {}", id, e.getMessage(), e);
            throw new GraphQLException("Failed to fetch category: " + e.getMessage());
        }
    }

    @QueryMapping
    public List<SavedFilter> savedFilters() {
        logger.info("Fetching all saved filters");
        try {
            List<SavedFilter> filters = savedFilterService.findByIsDeletedFalse();
            logger.info("Retrieved {} saved filters", filters.size());
            return filters;
        } catch (Exception e) {
            logger.error("Error fetching saved filters: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to fetch saved filters: " + e.getMessage());
        }
    }

    @QueryMapping
    public SavedFilter savedFilter(@Argument Integer id) {
        logger.info("Fetching saved filter with id: {}", id);
        try {
            SavedFilter filter = savedFilterService.findById(id).orElseThrow(() -> new GraphQLException("Filter not found"));
            return filter;
        } catch (Exception e) {
            logger.error("Error fetching saved filter with id {}: {}", id, e.getMessage(), e);
            throw new GraphQLException("Failed to fetch saved filter: " + e.getMessage());
        }
    }

    @MutationMapping
    public SavedFilter saveFilter(@Argument FilterInputDTO input) {
        logger.info("Saving filter with name: {}", input.getName());
        try {
            User user = userService.findByUsername(input.getUsername()).orElseThrow(() -> new IllegalArgumentException("Username " + input.getUsername() + " does not exist"));
            SavedFilter filter = new SavedFilter();
            filter.setName(input.getName().trim());
            if (input.getCategoryId() != null) {
                Category category = categoryService.findById(input.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Category ID " + input.getCategoryId() + " does not exist"));
                filter.setCategory(category);
            }
            filter.setUser(user);
            filter.setStartDate(input.getStartDate());
            filter.setEndDate(input.getEndDate());
            filter.setIsDeleted(false);
            filter.setFilterDeleted(input.getFilterDeleted());
            SavedFilter saved = savedFilterService.save(filter);
            logger.info("Saved filter with id: {} for user {}", saved.getId(), input.getUsername());
            return saved;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error saving filter: {}", e.getMessage());
            throw new GraphQLException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error saving filter: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to save filter: " + e.getMessage());
        }
    }

    @MutationMapping
    @Transactional
    public SavedFilter updateFilter(@Argument Integer id, @Argument FilterInputDTO input) {
        logger.info("Updating filter with id: {}", id);
        try {
            SavedFilter filter = savedFilterService.findByIdAndUserUsername(id, input.getUsername()).orElseThrow(() -> new IllegalArgumentException("Filter with ID " + id + " not found or you don't have permission to edit it."));
            filter.setName(input.getName().trim());
            filter.setStartDate(input.getStartDate());
            filter.setEndDate(input.getEndDate());
            filter.setFilterDeleted(input.getFilterDeleted());
            if (input.getCategoryId() != null) {
                Category category = categoryService.findById(input.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("Category ID " + input.getCategoryId() + " does not exist"));
                filter.setCategory(category);
            } else {
                filter.setCategory(null);
            }
            logger.info("Updated filter with id: {} for user {}, new name: {}", filter.getId(), input.getUsername(), filter.getName());
            return savedFilterService.save(filter); 
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating filter: {}", e.getMessage());
            throw new GraphQLException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating filter: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to update filter: " + e.getMessage());
        }
    }

    @MutationMapping
    public Boolean deleteFilter(@Argument Integer id) {
        logger.info("Soft-deleting filter with id: {}", id);
        try {
            SavedFilter filter = savedFilterService.findById(id).orElseThrow(() -> new IllegalArgumentException("Filter ID " + id + " does not exist"));
            filter.setIsDeleted(true);
            savedFilterService.save(filter);
            logger.info("Soft-deleted filter with id: {}", id);
            return true;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error soft-deleting filter with id {}: {}", id, e.getMessage());
            throw new GraphQLException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error soft-deleting filter with id {}: {}", id, e.getMessage(), e);
            throw new GraphQLException("Failed to soft-delete filter: " + e.getMessage());
        }
    }

    @QueryMapping
    public Integer getUserId(@Argument String usernameOrEmail) {
        logger.info("Fetching user ID for usernameOrEmail: {}", usernameOrEmail);
        try {
            User user = userService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() -> new IllegalArgumentException("No user found for username or email: " + usernameOrEmail));
            logger.info("Found user ID: {} for usernameOrEmail: {}", user.getId(), usernameOrEmail);
            return user.getId();
        } catch (IllegalArgumentException e) {
            logger.error("Validation error fetching user ID: {}", e.getMessage());
            throw new GraphQLException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching user ID: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to fetch user ID: " + e.getMessage());
        }
    }
}