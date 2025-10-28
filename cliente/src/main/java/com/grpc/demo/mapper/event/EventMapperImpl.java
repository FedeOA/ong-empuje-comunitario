package com.grpc.demo.mapper.event;

import java.util.ArrayList;
import java.util.List;

import com.grpc.demo.dto.out.DonationEventDTO;
import com.grpc.demo.service.event.DonationWithQuantity;
import org.springframework.stereotype.Component;

import com.grpc.demo.dto.out.EventResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.event.Event;

@Component
public class EventMapperImpl implements IMapper <Event, EventResponseDTO>{
    @Override
    public EventResponseDTO map(Event source) {

        List<String> users = new ArrayList<>();

        List<DonationEventDTO> donations = new ArrayList<>();

        for(int i = 0;i<source.getDonationsList().size();i ++){
            donations.add(mapDonation(source.getDonations(i)));
        }

        for(int i = 0;i<source.getUsersList().size();i++){
            users.add(source.getUsers(i));
        }

        return new EventResponseDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getFechaHora(),
                source.getIsPublished(),
                users,
                donations
        );
    }

    DonationEventDTO mapDonation(DonationWithQuantity donation){
        return new DonationEventDTO(
                donation.getCategoryId(),
                donation.getDescription(),
                donation.getQuantityUsed()
        );
    }
}
