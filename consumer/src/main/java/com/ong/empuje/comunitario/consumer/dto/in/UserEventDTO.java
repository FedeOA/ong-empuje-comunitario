package com.ong.empuje.comunitario.consumer.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserEventDTO {
    @JsonProperty("event_id")
    private Integer eventId;

    @JsonProperty("username")
    private String username;

    public UserEventDTO() {}

    public UserEventDTO(Integer eventId, String username) {
        this.eventId = eventId;
        this.username = username;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}