package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.in.DonationReportGroupDTO;
import com.ong.empuje.comunitario.web_services.dto.in.FilterInputDTO;
import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.Donation;
import com.ong.empuje.comunitario.web_services.model.SavedFilter;
import com.ong.empuje.comunitario.web_services.service.DonationService;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ong.empuje.comunitario.web_services.model.User;
import com.ong.empuje.comunitario.web_services.service.CategoryService;
import com.ong.empuje.comunitario.web_services.service.SavedFilterService;
import com.ong.empuje.comunitario.web_services.service.UserService;

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
        logger.info("Entering donationReport with filters: categoryId={}, startDate={}, endDate={}, deleted={}",
                categoryId, startDate, endDate, deleted);

        try {
            logger.debug("Parsing startDate: {}", startDate);
            LocalDateTime startDateTime = null;
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    logger.debug("startDate parsed successfully: {}", startDateTime);
                } catch (Exception e) {
                    logger.error("Invalid startDate format: {}", startDate, e);
                    throw new GraphQLException("Invalid startDate format: " + startDate);
                }
            }

            logger.debug("Parsing endDate: {}", endDate);
            LocalDateTime endDateTime = null;
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    logger.debug("endDate parsed successfully: {}", endDateTime);
                } catch (Exception e) {
                    logger.error("Invalid endDate format: {}", endDate, e);
                    throw new GraphQLException("Invalid endDate format: " + endDate);
                }
            }

            logger.debug("Validating date range: startDateTime={}, endDateTime={}", startDateTime, endDateTime);
            if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
                logger.error("End date {} is before start date {}", endDate, startDate);
                throw new GraphQLException("End date cannot be before start date");
            }

            logger.debug("Fetching donations with filters");
            List<Donation> donations = donationService.findByFilters(categoryId, startDateTime, endDateTime, deleted);
            logger.debug("Retrieved {} donations", donations.size());

            logger.debug("Grouping donations by category and deleted status");
            Map<Integer, Map<Boolean, List<Donation>>> grouped = donations.stream()
                    .collect(Collectors.groupingBy(
                            donation -> donation.getCategoryId() != null ? donation.getCategoryId() : 0,
                            Collectors.groupingBy(Donation::getDeleted)
                    ));
            logger.debug("Grouped donations into {} categories", grouped.size());

            List<DonationReportGroupDTO> result = new ArrayList<>();
            for (Integer catId : grouped.keySet()) {
                logger.debug("Processing category id: {}", catId);
                Category category = catId != 0 ? categoryService.findById(catId).orElse(null) : null;
                String categoryName = category != null ? category.getName() : "Desconocida";
                logger.debug("Category name for id {}: {}", catId, categoryName);

                for (Boolean del : grouped.get(catId).keySet()) {
                    logger.debug("Processing deleted status: {} for category id: {}", del, catId);
                    List<Donation> groupDonations = grouped.get(catId).get(del);
                    DonationReportGroupDTO group = new DonationReportGroupDTO();
                    group.setCategoryId(catId);
                    group.setCategoryName(categoryName);
                    group.setDeleted(del);
                    logger.debug("Calculating total quantity for group");
                    group.setTotalQuantity(groupDonations.stream()
                            .filter(d -> d.getQuantity() != null)
                            .mapToInt(Donation::getQuantity)
                            .sum());
                    group.setDonations(groupDonations);
                    result.add(group);
                    logger.debug("Added group to result: categoryId={}, deleted={}", catId, del);
                }
            }

            logger.info("Returning {} donation report groups", result.size());
            return result;
        } catch (GraphQLException e) {
            logger.error("Error processing donation report: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in donation report: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to process donation report: " + e.getMessage());
        }
    }

    @QueryMapping
    public List<Category> categories() {
        logger.info("Entering categories query");
        try {
            logger.debug("Fetching all categories");
            List<Category> categories = categoryService.findAll();
            logger.debug("Retrieved {} categories", categories.size());
            logger.info("Returning {} categories", categories.size());
            return categories;
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to fetch categories: " + e.getMessage());
        }
    }

    @QueryMapping
    public Category category(@Argument Integer id) {
        logger.info("Entering category query with id: {}", id);
        try {
            logger.debug("Fetching category with id: {}", id);
            Category category = categoryService.findById(id).orElse(null);
            if (category == null) {
                logger.warn("Category not found for id: {}", id);
            } else {
                logger.debug("Category found: id={}, name={}", id, category.getName());
            }
            logger.info("Returning category for id: {}", id);
            return category;
        } catch (Exception e) {
            logger.error("Error fetching category with id {}: {}", id, e.getMessage(), e);
            throw new GraphQLException("Failed to fetch category: " + e.getMessage());
        }
    }

    @QueryMapping
    public List<SavedFilter> savedFilters() {
        logger.info("Entering savedFilters query");
        try {
            logger.debug("Fetching all saved filters");
            List<SavedFilter> filters = savedFilterService.findByIsDeletedFalse();
            logger.debug("Retrieved {} saved filters", filters.size());
            logger.info("Returning {} saved filters", filters.size());
            return filters;
        } catch (Exception e) {
            logger.error("Error fetching saved filters: {}", e.getMessage(), e);
            throw new GraphQLException("Failed to fetch saved filters: " + e.getMessage());
        }
    }

    @QueryMapping
    public SavedFilter savedFilter(@Argument Integer id) {
        logger.info("Entering savedFilter query with id: {}", id);
        try {
            logger.debug("Fetching saved filter with id: {}", id);
            SavedFilter filter = savedFilterService.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Filter not found for id: {}", id);
                        return new GraphQLException("Filter not found");
                    });
            logger.debug("Filter found: id={}, name={}", id, filter.getName());
            logger.info("Returning saved filter for id: {}", id);
            return filter;
        } catch (Exception e) {
            logger.error("Error fetching saved filter with id {}: {}", id, e.getMessage(), e);
            throw new GraphQLException("Failed to fetch saved filter: " + e.getMessage());
        }
    }

    @MutationMapping
    public SavedFilter saveFilter(@Argument FilterInputDTO input) {
        logger.info("Entering saveFilter with name: {}", input.getName());
        try {
            logger.debug("Validating input for filter");
            if (input.getName() == null || input.getName().trim().isEmpty()) {
                logger.error("Filter name is null or empty");
                throw new IllegalArgumentException("Filter name is required");
            }
            if (input.getUsername() == null || input.getUsername().trim().isEmpty()) {
                logger.error("Username is null or empty");
                throw new IllegalArgumentException("Username is required");
            }
            logger.debug("Input validation passed");

            logger.debug("Fetching user with username: {}", input.getUsername());
            User user = userService.findByUsername(input.getUsername())
                    .orElseThrow(() -> {
                        logger.error("Username {} does not exist", input.getUsername());
                        return new IllegalArgumentException("Username " + input.getUsername() + " does not exist");
                    });
            logger.debug("User found: username={}", input.getUsername());

            SavedFilter filter = new SavedFilter();
            filter.setName(input.getName().trim());
            logger.debug("Set filter name: {}", filter.getName());

            if (input.getCategoryId() != null) {
                logger.debug("Fetching category with id: {}", input.getCategoryId());
                Category category = categoryService.findById(input.getCategoryId())
                        .orElseThrow(() -> {
                            logger.error("Category ID {} does not exist", input.getCategoryId());
                            return new IllegalArgumentException("Category ID " + input.getCategoryId() + " does not exist");
                        });
                filter.setCategory(category);
                logger.debug("Category set: id={}", input.getCategoryId());
            }

            filter.setUser(user);
            filter.setStartDate(input.getStartDate());
            filter.setEndDate(input.getEndDate());
            filter.setIsDeleted(false);
            logger.debug("Filter prepared for saving: name={}", filter.getName());

            logger.debug("Saving filter");
            SavedFilter saved = savedFilterService.save(filter);
            logger.debug("Filter saved: id={}", saved.getId());
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
        logger.info("Entering updateFilter with id: {}", id);
        try {
            logger.debug("Validating input for filter id: {}", id);
            if (id == null) {
                logger.error("Filter ID is null");
                throw new IllegalArgumentException("Filter ID is required");
            }
            if (input.getName() == null || input.getName().trim().isEmpty()) {
                logger.error("Filter name is null or empty");
                throw new IllegalArgumentException("Filter name is required");
            }
            if (input.getUsername() == null || input.getUsername().trim().isEmpty()) {
                logger.error("Username is null or empty");
                throw new IllegalArgumentException("Username is required");
            }
            logger.debug("Input validation passed for filter id: {}", id);

            logger.debug("Fetching filter with id: {} for user: {}", id, input.getUsername());
            SavedFilter filter = savedFilterService.findByIdAndUserUsername(id, input.getUsername())
                    .orElseThrow(() -> {
                        logger.error("Filter with ID {} not found or no permission for user {}", id, input.getUsername());
                        return new IllegalArgumentException("Filter with ID " + id + " not found or you don't have permission to edit it.");
                    });
            logger.debug("Filter fetched: id={}, name={}", id, filter.getName());

            logger.debug("Updating filter fields for id: {}", id);
            filter.setName(input.getName().trim());
            filter.setStartDate(input.getStartDate());
            filter.setEndDate(input.getEndDate());
            filter.setIsDeleted(input.getDeleted());
            logger.debug("Filter fields updated: name={}", filter.getName());

            if (input.getCategoryId() != null) {
                logger.debug("Fetching category with id: {}", input.getCategoryId());
                Category category = categoryService.findById(input.getCategoryId())
                        .orElseThrow(() -> {
                            logger.error("Category ID {} does not exist", input.getCategoryId());
                            return new IllegalArgumentException("Category ID " + input.getCategoryId() + " does not exist");
                        });
                filter.setCategory(category);
                logger.debug("Category set: id={}", input.getCategoryId());
            } else {
                filter.setCategory(null);
                logger.debug("Category set to null");
            }

            logger.info("Updated filter with id: {} for user {}, new name: {}", filter.getId(), input.getUsername(), filter.getName());
            return filter;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating filter with id {}: {}", id, e.getMessage());
            throw new GraphQLException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating filter with id {}: {}", id, e.getMessage(), e);
            throw new GraphQLException("Failed to update filter: " + e.getMessage());
        }
    }

    @MutationMapping
    public Boolean deleteFilter(@Argument Integer id) {
        logger.info("Entering deleteFilter with id: {}", id);
        try {
            logger.debug("Validating filter id: {}", id);
            if (id == null) {
                logger.error("Filter ID is null");
                throw new IllegalArgumentException("Filter ID is required");
            }

            logger.debug("Fetching filter with id: {}", id);
            SavedFilter filter = savedFilterService.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Filter ID {} does not exist", id);
                        return new IllegalArgumentException("Filter ID " + id + " does not exist");
                    });
            logger.debug("Filter found: id={}, name={}", id, filter.getName());

            logger.debug("Soft-deleting filter with id: {}", id);
            filter.setIsDeleted(true);
            savedFilterService.save(filter);
            logger.debug("Filter soft-deleted: id={}", id);

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
        logger.info("Entering getUserId with usernameOrEmail: {}", usernameOrEmail);
        try {
            logger.debug("Validating usernameOrEmail: {}", usernameOrEmail);
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                logger.error("Username or email is null or empty");
                throw new IllegalArgumentException("Username or email is required");
            }

            logger.debug("Fetching user with usernameOrEmail: {}", usernameOrEmail);
            User user = userService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> {
                        logger.error("No user found for username or email: {}", usernameOrEmail);
                        return new IllegalArgumentException("No user found for username or email: " + usernameOrEmail);
                    });
            logger.debug("User found: id={}, username={}", user.getId(), user.getUsername());

            logger.info("Returning user ID: {} for usernameOrEmail: {}", user.getId(), usernameOrEmail);
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