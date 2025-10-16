package com.ong.empuje.comunitario.web_services.dto;

import java.util.List;

public class EventsDonationsResponseDTO {

    private String name;
    private String date;
    private String description;
    private List<DonationDTO> donations;

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public List<DonationDTO> getDonations() {return donations;}

    public void setDonations(List<DonationDTO> donations) {this.donations = donations;}
}
