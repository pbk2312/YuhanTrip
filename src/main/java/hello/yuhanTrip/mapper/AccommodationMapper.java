package hello.yuhanTrip.mapper;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.dto.accommodation.AccommodationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AccommodationMapper {

    AccommodationMapper INSTANCE = Mappers.getMapper(AccommodationMapper.class);

    // Entity to DTO
    AccommodationDTO toDTO(Accommodation accommodation);

    // DTO to Entity
    Accommodation toEntity(AccommodationDTO accommodationDTO);

    // List<Entity> to List<DTO>
    List<AccommodationDTO> toDTOList(List<Accommodation> accommodations);

    // List<DTO> to List<Entity>
    List<Accommodation> toEntityList(List<AccommodationDTO> accommodationDTOs);
}