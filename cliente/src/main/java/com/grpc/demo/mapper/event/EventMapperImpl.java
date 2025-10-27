package com.grpc.demo.mapper.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.grpc.demo.dto.in.DonationDTO;
import com.grpc.demo.dto.out.DonationResponseDTO;
import com.grpc.demo.dto.out.EventResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.event.Event;
import static com.grpc.demo.enums.Category.UTILES_ESCOLARES;
import static com.grpc.demo.enums.Category.fromId;
import static com.grpc.demo.enums.Category.idFromName;
import com.grpc.demo.service.donation.Donation;

@Component
public class EventMapperImpl implements IMapper <Event, EventResponseDTO>{
    @Override
    public EventResponseDTO map(Event source) {

        List<String> users = new ArrayList<>();

        List<DonationResponseDTO> donations = new ArrayList<>();

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

    DonationResponseDTO mapDonation(Donation donation)
    {
        String category;

        if (donation.getCategoria() == idFromName(UTILES_ESCOLARES.name()))
        {
            category = "UTILES ESCOLARES";
        }
        else
        {
            category = fromId(donation.getCategoria()).name();
        }

        return new DonationResponseDTO(
            donation.getId(),
            category,
            donation.getDescription(),
            donation.getCantidad(),
            donation.getUsername()
        );
    }
}
