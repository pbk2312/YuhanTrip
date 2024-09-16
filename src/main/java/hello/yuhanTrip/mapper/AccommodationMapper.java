package hello.yuhanTrip.mapper;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.dto.accommodation.AccommodationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AccommodationMapper {

    AccommodationMapper INSTANCE = Mappers.getMapper(AccommodationMapper.class);

    AccommodationDTO toDTO(Accommodation accommodation);


    // List mappings can remain as is
    List<AccommodationDTO> toDTOList(List<Accommodation> accommodations);
}