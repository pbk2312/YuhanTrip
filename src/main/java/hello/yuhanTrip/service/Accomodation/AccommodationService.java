package hello.yuhanTrip.service.Accomodation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hello.yuhanTrip.domain.*;
import hello.yuhanTrip.dto.AccommodationRegisterDTO;
import hello.yuhanTrip.dto.RoomDTO;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.MemberRepository;
import hello.yuhanTrip.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class AccommodationService {


    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;


    @Value("${upload.dir}")
    private String uploadDir;


    public Accommodation getAccommodationInfo(Long id) {

        Accommodation accommodation = accommodationRepository.findById(id).orElseThrow(() -> new RuntimeException("존재하지 않는 숙소입니다."));

        log.info("숙소 이름 = {}", accommodation.getTitle());

        return accommodation;


    }


    public Room getRoomInfo(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("객실 정보 없음"));
        return room;
    }




    @JsonIgnore
    public Accommodation registerAccommodation(Long memberId, AccommodationRegisterDTO dto) throws IOException {
        // 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 호스트 권한 확인
        if (member.getMemberRole() != MemberRole.ROLE_HOST) {
            throw new IllegalStateException("Only HOST members can register accommodations");
        }

        // Accommodation 객체 생성 및 기본 정보 설정
        Accommodation accommodation = new Accommodation();
        accommodation.setAddr1(dto.getAddr1());
        accommodation.setAddr2(dto.getAddr2());
        accommodation.setFirstimage(null); // 처음엔 이미지 경로를 null로 설정
        accommodation.setFirstimage2(null);
        accommodation.setTel(dto.getTel());
        accommodation.setTitle(dto.getTitle());
        accommodation.setSigungucode(dto.getSigungucode());
        accommodation.setMember(member);
        accommodation.setStatus(AccommodationApplyStatus.PENDING);
        accommodation.setAreacode(dto.getSigungucode());

        // 이미지 저장
        List<String> imagePaths = new ArrayList<>();
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (MultipartFile image : dto.getImages()) {
                String imagePath = saveImage(image);
                imagePaths.add(imagePath);
            }
        }
        if (!imagePaths.isEmpty()) {
            accommodation.setFirstimage(imagePaths.get(0)); // 첫 번째 이미지를 대표 이미지로 설정
            if (imagePaths.size() > 1) {
                accommodation.setFirstimage2(imagePaths.get(1)); // 두 번째 이미지를 추가 이미지로 설정
            }
        }

        log.info("객실정보 저장 시도....");

        // 객실 정보 저장
        if (dto.getRooms() != null && !dto.getRooms().isEmpty()) {
            for (RoomDTO roomDTO : dto.getRooms()) {
                Room room = new Room();
                room.setRoomNo(roomDTO.getRoomNo());
                room.setRoomNm(roomDTO.getRoomNm());
                room.setRoomType(roomDTO.getRoomType());
                room.setMaxOccupancy(roomDTO.getMaxOccupancy());
                room.setRoomArea(roomDTO.getRoomArea());
                room.setPrice(roomDTO.getPrice());
                room.setAmenities(roomDTO.getAmenities());
                room.setRoomIntr(roomDTO.getRoomIntr());

                // 객실 이미지 저장
                if (roomDTO.getRoomImg() != null && !roomDTO.getRoomImg().isEmpty()) {
                    String roomImagePath = saveImage(roomDTO.getRoomImg());
                    room.setRoomImgUrl(roomImagePath);
                }

                room.setSmokingYn(roomDTO.getSmokingYn());
                room.setBreakfastInclYn(roomDTO.getBreakfastInclYn());
                room.setCheckInTime(roomDTO.getCheckInTime());
                room.setCheckOutTime(roomDTO.getCheckOutTime());
                room.setAccommodation(accommodation); // 객실과 숙소 연관 설정
                accommodation.getRooms().add(room); // 숙소에 객실 추가
            }
        }

        return accommodationRepository.save(accommodation);
    }


    public Page<Accommodation> getAccommodations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatus(
                AccommodationApplyStatus.APPROVED,
                pageable
        );
    }


    // 지역 코드를 기반으로 숙소를 페이지네이션하여 조회
    public Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findByAreacode(areaCode, pageable);
    }



    // 해당 날짜에 예약이 가능한 승인된 숙소들 보여주기
    public Page<Accommodation> getAvailableAccommodations(String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size) {
        return accommodationRepository.findAvailableAccommodations(
                AccommodationApplyStatus.APPROVED, // 추가된 상태 필터
                areaCode,
                checkInDate,
                checkOutDate,
                numGuests,
                PageRequest.of(page, size)
        );
    }

    // 새로운 findAvailableAccommodationsByAverageRating 메서드
    public Page<Accommodation> getAvailableAccommodationsByAverageRating(
            String areaCode,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numGuests,
            int page,
            int size
    ) {
        return accommodationRepository.findAvailableAccommodationsByAverageRating(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests,PageRequest.of(page, size)
        );
    }





    /**
     * 상태에 따라 숙소를 조회하고, 평점과 리뷰 수로 정렬하여 반환합니다.
     *
     */
    public Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(
            Pageable pageable) {
        return accommodationRepository.findAllByStatusWithSorting(AccommodationApplyStatus.APPROVED, pageable);
    }






    public List<Room> getAvailableRoomsByAccommodation(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<ReservationStatus> excludedStatuses = Arrays.asList(
                ReservationStatus.CANCELLED,
                ReservationStatus.REJECTED
        );

        return roomRepository.findAvailableRoomsByAccommodation(
                accommodationId,
                checkInDate,
                checkOutDate,
                excludedStatuses
        );
    }

    /**
     * 높은 가격순으로 예약 가능한 숙소 조회
     */
    public Page<Accommodation> getAvailableAccommodationsOrderByPriceDesc(
            String areaCode,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numGuests,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAvailableAccommodationsOrderByAveragePriceDesc(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, pageable
        );
    }

    /**
     * 낮은 가격순으로 예약 가능한 숙소 조회
     */
    public Page<Accommodation> getAvailableAccommodationsOrderByPriceAsc(
            String areaCode,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numGuests,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAvailableAccommodationsOrderByAveragePriceAsc(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, pageable
        );
    }

    /**
     * 전체 숙소 중 높은 가격순으로 조회
     */
    public Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceDesc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }

    /**
     * 전체 숙소 중 낮은 가격순으로 조회
     */
    public Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceAsc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }

    // 검색 결과를 Page로 반환
    public Page<Accommodation> searchByTitle(String title, Pageable pageable) {
        return accommodationRepository.findByTitleContainingIgnoreCase(title, pageable);
    }


    private String saveImage(MultipartFile image) throws IOException {

        // 이미지 저장 디렉토리 설정
        Path uploadPath = Paths.get(uploadDir);
        log.info("업로드 디렉토리: {}", uploadPath);

        try {
            // 디렉토리 존재 여부 확인 및 생성
            if (!Files.exists(uploadPath)) {
                log.info("디렉토리가 존재하지 않으므로 생성합니다.");
                Files.createDirectories(uploadPath);
            }

            // 파일 이름 생성
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            log.info("파일 경로: {}", filePath);

            // 파일 저장
            Files.write(filePath, image.getBytes());
            log.info("파일이 성공적으로 저장되었습니다: {}", fileName);

            // 저장된 이미지의 URL 반환
            return "/upload/" + fileName;
        } catch (IOException e) {
            log.error("파일 저장 오류: {}", e.getMessage(), e);
            throw new IOException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

}