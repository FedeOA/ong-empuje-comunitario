package com.ong.empuje.comunitario.web_services.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventFilterResponseDTO {

    private String name;

    private String distribution;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("search_username")
    private String searchUsername;

    public String getName() {return name;}

    public String getDistribution() {return distribution;}

    public String getStartDate() {return startDate;}

    public String getEndDate() {return endDate;}

    public String getSearchUsername() {return searchUsername;}

    public void setName(String name) {this.name = name;}

    public void setDistribution(String distribution) {this.distribution = distribution;}

    public void setStartDate(String startDate) {this.startDate = startDate;}

    public void setEndDate(String endDate) {this.endDate = endDate;}

    public void setSearchUsername(String searchUsername) {this.searchUsername = searchUsername;}
}
