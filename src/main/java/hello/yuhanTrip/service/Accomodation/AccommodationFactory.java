package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.AccommodationApplyStatus;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.domain.Room;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import hello.yuhanTrip.dto.RoomDTO;
import hello.yuhanTrip.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccommodationFactory {

    private final ImageService imageService;

    public Accommodation createAccommodationFromDTO(AccommodationRegisterDTO dto, Member member) throws IOException {
        // Accommodation 객체 생성 및 기본 정보 설정
        Accommodation accommodation = new Accommodation();
        accommodation.setAddr1(dto.getAddr1());
        accommodation.setAddr2(dto.getAddr2());
        accommodation.setTel(dto.getTel());
        accommodation.setTitle(dto.getTitle());
        accommodation.setSigungucode(dto.getSigungucode());
        accommodation.setMember(member);
        accommodation.setStatus(AccommodationApplyStatus.PENDING);
        accommodation.setAreacode(dto.getSigungucode());

        List<String> imagePaths = imageService.saveImages(dto.getImages());
        if (!imagePaths.isEmpty()) {
            accommodation.setFirstimage(imagePaths.get(0));
            if (imagePaths.size() > 1) {
                accommodation.setFirstimage2(imagePaths.get(1));
            }
        }

        // 객실 정보 저장
        if (dto.getRooms() != null && !dto.getRooms().isEmpty()) {
            for (RoomDTO roomDTO : dto.getRooms()) {
                Room room = createRoomFromDTO(roomDTO, accommodation);
                accommodation.getRooms().add(room); // 숙소에 객실 추가
            }
        }

        return accommodation;
    }

    public Room createRoomFromDTO(RoomDTO roomDTO, Accommodation accommodation) {
        Room room = new Room();
        room.setRoomNo(roomDTO.getRoomNo());
        room.setRoomNm(roomDTO.getRoomNm());
        room.setRoomType(roomDTO.getRoomType());
        room.setMaxOccupancy(roomDTO.getMaxOccupancy());
        room.setRoomArea(roomDTO.getRoomArea());
        room.setPrice(roomDTO.getPrice());
        room.setAmenities(roomDTO.getAmenities());
        room.setRoomIntr(roomDTO.getRoomIntr());

        if (roomDTO.getRoomImg() != null && !roomDTO.getRoomImg().isEmpty()) {
            try {
                String roomImagePath = imageService.saveImage(roomDTO.getRoomImg());
                room.setRoomImgUrl(roomImagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save room image", e);
            }
        }

        room.setSmokingYn(roomDTO.getSmokingYn());
        room.setBreakfastInclYn(roomDTO.getBreakfastInclYn());
        room.setCheckInTime(roomDTO.getCheckInTime());
        room.setCheckOutTime(roomDTO.getCheckOutTime());
        room.setAccommodation(accommodation);
        return room;
    }
}
