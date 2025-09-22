package com.grpc.demo.mapper.donations;

import com.grpc.demo.dto.out.DonationResponseDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.donation.Donation;
import org.springframework.stereotype.Component;

import static com.grpc.demo.enums.Category.*;

@Component
public class DonationMapperImpl implements IMapper<Donation, DonationResponseDTO> {
    @Override
    public DonationResponseDTO map(Donation source) {

        String category;

        if( source.getCategoria() == idFromName(UTILES_ESCOLARES.name())){

            category = "UTILES ESCOLARES";
        }else{
            category = fromId(source.getCategoria()).name();
        }

        return new DonationResponseDTO(
                source.getId(),
                category,
                source.getDescription(),
                source.getCantidad(),
                source.getUsername()
                );
    }
}
