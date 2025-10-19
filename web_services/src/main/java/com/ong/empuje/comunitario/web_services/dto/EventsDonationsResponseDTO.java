package com.ong.empuje.comunitario.web_services.dto;

import java.util.List;

public class EventsDonationsResponseDTO {

    private String name;
    private String datetime;
    private String description;
    private List<DonationDTO> donations;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getDate() {return datetime;}

    public void setDate(String datetime) {this.datetime = datetime;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public List<DonationDTO> getDonations() {return donations;}

    public void setDonations(List<DonationDTO> donations) {this.donations = donations;}
}
