package com.grpc.demo.mapper.donations;

import com.grpc.demo.dto.in.DonationDTO;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.donation.Donation;
import org.springframework.stereotype.Component;

import static com.grpc.demo.enums.Category.*;

@Component
public class DonationMapperImpl implements IMapper<Donation, DonationDTO> {
    @Override
    public DonationDTO map(Donation source) {

        String category;

        if( source.getCategoria() == idFromName(UTILES_ESCOLARES.name())){

            category = "UTILES ESCOLARES";
        }else{
            category = fromId(source.getCategoria()).name();
        }

        return new DonationDTO(
                category,
                source.getDescription(),
                source.getCantidad(),
                source.getUsername()
                );
    }
}
