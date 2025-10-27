package com.ong.empuje.comunitario.web_services.repository.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ong.empuje.comunitario.web_services.dto.in.DonationDTO;
import com.ong.empuje.comunitario.web_services.dto.in.EventFilterDTO;
import com.ong.empuje.comunitario.web_services.dto.out.EventFilterResponseDTO;
import com.ong.empuje.comunitario.web_services.dto.out.EventsDonationsResponseDTO;
import com.ong.empuje.comunitario.web_services.enums.DonationDistributionFilter;
import com.ong.empuje.comunitario.web_services.repository.EventJdbcRepository;

@Repository
public class JdbcRepository implements EventJdbcRepository {

    private static final String saveSql = """
        INSERT INTO event_filter (distribution, end_date, name, start_date, username, user_id)
        VALUES (:distribution, :endDate, :name, :startDate, :searchUsername,
                (SELECT id FROM users WHERE username = :username))
    """;

    private static final String getFilterSql = """
        SELECT ef.distribution, ef.end_date, ef.name, ef.start_date, ef.username
        FROM event_filter ef
        JOIN users u ON ef.user_id = u.id
        WHERE u.username = :username
    """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<EventsDonationsResponseDTO> findFilteredEvents(String username, String startDate, String endDate, DonationDistributionFilter distribution) {
        StringBuilder sql = new StringBuilder("""
            SELECT e.id AS event_id, e.name, e.event_datetime, e.description,
                   d.category_id, d.description AS donation_description, ed.quantity_used
            FROM events e
            LEFT JOIN event_donations ed ON ed.event_id = e.id
            LEFT JOIN donations d ON d.id = ed.donation_id AND d.category_id IS NOT NULL
            JOIN user_events eu ON eu.event_id = e.id
            JOIN users u ON u.id = eu.user_id
            WHERE u.username = :username
            AND (d.id IS NULL OR d.category_id IS NOT NULL)
        """);

        Map<String, Object> params = new HashMap<>();
        params.put("username", username);

        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND e.event_datetime >= :startDate");
            params.put("startDate", LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND e.event_datetime <= :endDate");
            params.put("endDate", LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        if (distribution != null) {
            switch (distribution) {
                case YES -> sql.append(" AND ed.donation_id IS NOT NULL AND d.category_id IS NOT NULL");
                case NO -> sql.append(" AND ed.donation_id IS NULL");
                default -> {} // No additional filter
            }
        }

        try {
            return namedParameterJdbcTemplate.query(sql.toString(), params, rs -> {
                Map<Long, EventsDonationsResponseDTO> eventMap = new LinkedHashMap<>();

                while (rs.next()) {
                    Long eventId = rs.getLong("event_id");
                    String name = rs.getString("name");
                    String date = rs.getString("event_datetime");
                    String description = rs.getString("description");

                    EventsDonationsResponseDTO event = eventMap.computeIfAbsent(eventId, id -> {
                        EventsDonationsResponseDTO dto = new EventsDonationsResponseDTO();
                        dto.setName(name);
                        dto.setDate(date);
                        dto.setDescription(description);
                        dto.setDonations(new ArrayList<>());
                        return dto;
                    });

                    Integer categoryId = rs.getObject("category_id", Integer.class);
                    String description2 = rs.getString("donation_description");
                    String quantity = rs.getString("quantity_used");

                    if (categoryId != null && description2 != null && quantity != null) {
                        DonationDTO donation = new DonationDTO();
                        donation.setCategory(categoryId.toString());
                        donation.setDescription(description2);
                        donation.setQuantity(quantity);
                        event.getDonations().add(donation);
                    }
                }

                return new ArrayList<>(eventMap.values());
            });
        } catch (Exception e) {
            System.err.println("Exception in findFilteredEvents: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void saveFilter(EventFilterDTO eventFilter) {
        if (eventFilter == null || eventFilter.username() == null || eventFilter.username().isBlank() || eventFilter.searchUsername() == null || eventFilter.searchUsername().isBlank()) {
            throw new IllegalArgumentException("El filtro y el nombre de usuario son obligatorios");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("distribution", eventFilter.distribution())
                .addValue("endDate", eventFilter.endDate())
                .addValue("name", eventFilter.name())
                .addValue("startDate", eventFilter.startDate())
                .addValue("username", eventFilter.username())
                .addValue("searchUsername", eventFilter.searchUsername());

        try {
            namedParameterJdbcTemplate.update(saveSql, params);
        } catch (Exception e) {
            System.err.println("Exception in saveFilter: " + e.getMessage());
        }
    }

    @Override
    public List<EventFilterResponseDTO> getFilters(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username);

        try {
            return namedParameterJdbcTemplate.query(getFilterSql, params, rs -> {
                List<EventFilterResponseDTO> filters = new ArrayList<>();
                while (rs.next()) {
                    EventFilterResponseDTO dto = new EventFilterResponseDTO();
                    dto.setDistribution(rs.getString("distribution"));
                    dto.setName(rs.getString("name"));
                    dto.setSearchUsername(rs.getString("username"));
                    Date startDate = rs.getDate("start_date");
                    dto.setStartDate(startDate != null ? startDate.toString() : null);
                    Date endDate = rs.getDate("end_date");
                    dto.setEndDate(endDate != null ? endDate.toString() : null);
                    filters.add(dto);
                }
                return filters;
            });
        } catch (Exception e) {
            System.err.println("Exception in getFilters: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}