// web_services/src/main/java/com/ong/empuje/comunitario/web_services/controller/DonationReportController.java
package com.ong.empuje.comunitario.web_services.controller;

import com.ong.empuje.comunitario.web_services.dto.DonationReportGroup;
import com.ong.empuje.comunitario.web_services.model.Category;
import com.ong.empuje.comunitario.web_services.model.Donation;
import com.ong.empuje.comunitario.web_services.model.SavedFilter;
import com.ong.empuje.comunitario.web_services.repository.CategoryRepository;
import com.ong.empuje.comunitario.web_services.repository.DonationRepository;
import com.ong.empuje.comunitario.web_services.repository.SavedFilterRepository;
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
import com.ong.empuje.comunitario.web_services.dto.FilterInput;

@Controller
public class DonationReportController {
    private static final Logger logger = LoggerFactory.getLogger(DonationReportController.class);

    private final DonationRepository donationRepository;
    private final CategoryRepository categoryRepository;
    private final SavedFilterRepository savedFilterRepository;

    public DonationReportController(
            DonationRepository donationRepository,
            CategoryRepository categoryRepository,
            SavedFilterRepository savedFilterRepository) {
        this.donationRepository = donationRepository;
        this.categoryRepository = categoryRepository;
        this.savedFilterRepository = savedFilterRepository;
    }

    @QueryMapping
    public List<DonationReportGroup> donationReport(
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
                } catch (Exception e) {
                    logger.error("Invalid startDate format: {}", startDate, e);
                    throw new IllegalArgumentException("Invalid startDate format: " + startDate);
                }
            }

            LocalDateTime endDateTime = null;
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    logger.error("Invalid endDate format: {}", endDate, e);
                    throw new IllegalArgumentException("Invalid endDate format: " + endDate);
                }
            }

            if (startDateTime != null && endDateTime != null && startDateTime.isAfter(endDateTime)) {
                logger.error("End date {} is before start date {}", endDate, startDate);
                throw new IllegalArgumentException("End date cannot be before start date");
            }

            List<Donation> donations = donationRepository.findByFilters(categoryId, startDateTime, endDateTime, deleted);
            logger.debug("Retrieved {} donations", donations.size());

            Map<Integer, Map<Boolean, List<Donation>>> grouped = donations.stream()
                    .collect(Collectors.groupingBy(
                            donation -> donation.getCategoryId() != null ? donation.getCategoryId() : 0, // Handle null categoryId
                            Collectors.groupingBy(Donation::getDeleted)
                    ));

            List<DonationReportGroup> result = new ArrayList<>();
            for (Integer catId : grouped.keySet()) {
                Category category = catId != 0 ? categoryRepository.findById(catId).orElse(null) : null;
                String categoryName = category != null ? category.getName() : "Desconocida";

                for (Boolean del : grouped.get(catId).keySet()) {
                    List<Donation> groupDonations = grouped.get(catId).get(del);
                    DonationReportGroup group = new DonationReportGroup();
                    group.setCategoryId(catId);
                    group.setCategoryName(categoryName);
                    group.setDeleted(del);
                    group.setTotalQuantity(groupDonations.stream()
                            .filter(d -> d.getQuantity() != null) // Handle null quantity
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
            throw new RuntimeException("Failed to process donation report: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<Category> categories() {
        logger.info("Fetching all categories");
        try {
            List<Category> categories = categoryRepository.findAll();
            logger.info("Retrieved {} categories", categories.size());
            return categories;
        } catch (Exception e) {
            logger.error("Error fetching categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch categories: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public Category category(@Argument Integer id) {
        logger.info("Fetching category with id: {}", id);
        try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                logger.warn("Category not found for id: {}", id);
            }
            return category;
        } catch (Exception e) {
            logger.error("Error fetching category with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch category: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<SavedFilter> savedFilters() {
        logger.info("Fetching all saved filters");
        try {
            List<SavedFilter> filters = savedFilterRepository.findAll();
            logger.info("Retrieved {} saved filters", filters.size());
            return filters;
        } catch (Exception e) {
            logger.error("Error fetching saved filters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch saved filters: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public SavedFilter savedFilter(@Argument Integer id) {
        logger.info("Fetching saved filter with id: {}", id);
        try {
            SavedFilter filter = savedFilterRepository.findById(id).orElse(null);
            if (filter == null) {
                logger.warn("Saved filter not found for id: {}", id);
            }
            return filter;
        } catch (Exception e) {
            logger.error("Error fetching saved filter with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch saved filter: " + e.getMessage(), e);
        }
    }

    @MutationMapping
    public SavedFilter saveFilter(@Argument FilterInput input) {
        logger.info("Saving filter with name: {}", input.getName());
        try {
            SavedFilter filter = new SavedFilter();
            filter.setName(input.getName());

            if (input.getCategoryId() != null) {
                Category category = categoryRepository.findById(input.getCategoryId())
                        .orElseThrow(() -> {
                            logger.error("Category not found for id: {}", input.getCategoryId());
                            return new RuntimeException("Category not found");
                        });
                filter.setCategory(category);
            }

            filter.setStartDate(input.getStartDate());
            filter.setEndDate(input.getEndDate());
            filter.setDeleted(input.getDeleted());

            SavedFilter saved = savedFilterRepository.save(filter);
            logger.info("Saved filter with id: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Error saving filter: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save filter: " + e.getMessage(), e);
        }
    }

    @MutationMapping
    public SavedFilter updateFilter(@Argument Integer id, @Argument FilterInput input) {
        logger.info("Updating filter with id: {}", id);
        try {
            SavedFilter filter = savedFilterRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Filter not found for id: {}", id);
                        return new RuntimeException("Filter not found");
                    });

            filter.setName(input.getName());

            if (input.getCategoryId() != null) {
                Category category = categoryRepository.findById(input.getCategoryId())
                        .orElseThrow(() -> {
                            logger.error("Category not found for id: {}", input.getCategoryId());
                            return new RuntimeException("Category not found");
                        });
                filter.setCategory(category);
            } else {
                filter.setCategory(null);
            }

            filter.setStartDate(input.getStartDate());
            filter.setEndDate(input.getEndDate());
            filter.setDeleted(input.getDeleted());

            SavedFilter updated = savedFilterRepository.save(filter);
            logger.info("Updated filter with id: {}", updated.getId());
            return updated;
        } catch (Exception e) {
            logger.error("Error updating filter with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update filter: " + e.getMessage(), e);
        }
    }

    @MutationMapping
    public Boolean deleteFilter(@Argument Integer id) {
        logger.info("Deleting filter with id: {}", id);
        try {
            SavedFilter filter = savedFilterRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Filter not found for id: {}", id);
                        return new RuntimeException("Filter not found");
                    });

            savedFilterRepository.deleteById(id);
            logger.info("Deleted filter with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting filter with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete filter: " + e.getMessage(), e);
        }
    }
}