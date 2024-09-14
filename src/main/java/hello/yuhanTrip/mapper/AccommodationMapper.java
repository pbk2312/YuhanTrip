package hello.yuhanTrip.mapper;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.dto.accommodation.AccommodationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AccommodationMapper {

    AccommodationMapper INSTANCE = Mappers.getMapper(AccommodationMapper.class);

    @Mapping(target = "roomImg", ignore = true)
    AccommodationDTO toDTO(Accommodation accommodation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomImgUrl", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "memberLikes", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Accommodation toEntity(AccommodationDTO accommodationDTO);

    // List mappings can remain as is
    List<AccommodationDTO> toDTOList(List<Accommodation> accommodations);
    List<Accommodation> toEntityList(List<AccommodationDTO> accommodationDTOs);
}