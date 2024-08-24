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

    /**
     * 숙소 정보를 가져옵니다.
     * @param id 숙소 ID
     * @return 숙소 정보
     */
    public Accommodation getAccommodationInfo(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 숙소입니다."));
    }

    /**
     * 객실 정보를 가져옵니다.
     * @param id 객실 ID
     * @return 객실 정보
     */
    public Room getRoomInfo(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("객실 정보 없음"));
    }

    /**
     * 새로운 숙소를 등록합니다.
     * @param memberId 호스트의 ID
     * @param dto 숙소 등록 DTO
     * @return 등록된 숙소
     */
    @JsonIgnore
    public Accommodation registerAccommodation(Long memberId, AccommodationRegisterDTO dto) throws IOException {
        // 멤버 조회 및 권한 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        if (member.getMemberRole() != MemberRole.ROLE_HOST) {
            throw new IllegalStateException("Only HOST members can register accommodations");
        }

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

        // 이미지 저장
        List<String> imagePaths = saveImages(dto.getImages());
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
                Room room = createRoomFromDTO(roomDTO, accommodation);
                accommodation.getRooms().add(room); // 숙소에 객실 추가
            }
        }

        return accommodationRepository.save(accommodation);
    }

    private List<String> saveImages(List<MultipartFile> images) throws IOException {
        List<String> imagePaths = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imagePath = saveImage(image);
                imagePaths.add(imagePath);
            }
        }
        return imagePaths;
    }

    private Room createRoomFromDTO(RoomDTO roomDTO, Accommodation accommodation) throws IOException {
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
        return room;
    }

    /**
     * 승인된 숙소를 페이지네이션하여 반환합니다.
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 숙소 목록
     */
    public Page<Accommodation> getAccommodations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatus(AccommodationApplyStatus.APPROVED, pageable);
    }

    /**
     * 지역 코드에 따라 숙소를 페이지네이션하여 조회합니다.
     * @param areaCode 지역 코드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 숙소 목록
     */
    public Page<Accommodation> getAccommodationsByAreaCode(String areaCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findByAreacode(areaCode, pageable);
    }

    /**
     * 해당 날짜에 예약이 가능한 승인된 숙소를 보여줍니다.
     * @param areaCode 지역 코드
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param numGuests 인원 수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 예약 가능한 숙소 목록
     */
    public Page<Accommodation> getAvailableAccommodations(
            String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size) {
        return accommodationRepository.findAvailableAccommodations(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, PageRequest.of(page, size)
        );
    }

    /**
     * 평점 순으로 예약 가능한 숙소를 조회합니다.
     * @param areaCode 지역 코드
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param numGuests 인원 수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 평점 순 숙소 목록
     */
    public Page<Accommodation> getAvailableAccommodationsByAverageRating(
            String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size) {
        return accommodationRepository.findAvailableAccommodationsByAverageRating(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, PageRequest.of(page, size)
        );
    }

    /**
     * 상태에 따라 숙소를 조회하고, 평점과 리뷰 수로 정렬하여 반환합니다.
     * @param pageable 페이지 정보
     * @return 페이지네이션된 숙소 목록
     */
    public Page<Accommodation> getAvailableAccommodationsSortedByRatingAndReview(Pageable pageable) {
        return accommodationRepository.findAllByStatusWithSorting(AccommodationApplyStatus.APPROVED, pageable);
    }

    /**
     * 특정 숙소의 예약 가능한 객실을 조회합니다.
     * @param accommodationId 숙소 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @return 예약 가능한 객실 목록
     */
    public List<Room> getAvailableRoomsByAccommodation(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<ReservationStatus> excludedStatuses = Arrays.asList(
                ReservationStatus.CANCELLED,
                ReservationStatus.REJECTED
        );
        return roomRepository.findAvailableRoomsByAccommodation(
                accommodationId, checkInDate, checkOutDate, excludedStatuses
        );
    }

    /**
     * 높은 가격순으로 예약 가능한 숙소를 조회합니다.
     * @param areaCode 지역 코드
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param numGuests 인원 수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 높은 가격순 숙소 목록
     */
    public Page<Accommodation> getAvailableAccommodationsOrderByPriceDesc(
            String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAvailableAccommodationsOrderByAveragePriceDesc(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, pageable
        );
    }

    /**
     * 낮은 가격순으로 예약 가능한 숙소를 조회합니다.
     * @param areaCode 지역 코드
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param numGuests 인원 수
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 낮은 가격순 숙소 목록
     */
    public Page<Accommodation> getAvailableAccommodationsOrderByPriceAsc(
            String areaCode, LocalDate checkInDate, LocalDate checkOutDate, int numGuests, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAvailableAccommodationsOrderByAveragePriceAsc(
                AccommodationApplyStatus.APPROVED, areaCode, checkInDate, checkOutDate, numGuests, pageable
        );
    }

    /**
     * 전체 숙소 중 높은 가격순으로 조회합니다.
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 높은 가격순 숙소 목록
     */
    public Page<Accommodation> getAllAccommodationsOrderByPriceDesc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceDesc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }

    /**
     * 전체 숙소 중 낮은 가격순으로 조회합니다.
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 낮은 가격순 숙소 목록
     */
    public Page<Accommodation> getAllAccommodationsOrderByPriceAsc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAllByStatusOrderByAveragePriceAsc(
                AccommodationApplyStatus.APPROVED, pageable
        );
    }

    /**
     * 숙소 제목으로 검색하여 결과를 반환합니다.
     * @param title 숙소 제목
     * @param pageable 페이지 정보
     * @return 검색 결과 페이지
     */
    public Page<Accommodation> searchByTitle(String title, Pageable pageable) {
        return accommodationRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    /**
     * 이미지를 저장하고 URL을 반환합니다.
     * @param image 저장할 이미지
     * @return 이미지 URL
     * @throws IOException 이미지 저장 중 오류 발생 시
     */
    private String saveImage(MultipartFile image) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        log.info("업로드 디렉토리: {}", uploadPath);

        if (!Files.exists(uploadPath)) {
            log.info("디렉토리가 존재하지 않으므로 생성합니다.");
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        log.info("파일 경로: {}", filePath);

        Files.write(filePath, image.getBytes());
        log.info("파일이 성공적으로 저장되었습니다: {}", fileName);

        return "/upload/" + fileName;
    }
}
