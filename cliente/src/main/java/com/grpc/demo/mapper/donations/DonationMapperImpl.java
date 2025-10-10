package com.grpc.demo.mapper.donations;

import org.springframework.stereotype.Component;

import com.grpc.demo.dto.out.DonationResponseDTO;
import static com.grpc.demo.enums.Category.UTILES_ESCOLARES;
import static com.grpc.demo.enums.Category.fromId;
import static com.grpc.demo.enums.Category.idFromName;
import com.grpc.demo.mapper.IMapper;
import com.grpc.demo.service.donation.Donation;

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
