package hello.yuhanTrip.service.Accomodation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.accommodation.AccommodationApplyStatus;
import hello.yuhanTrip.domain.accommodation.Review;
import hello.yuhanTrip.domain.accommodation.Room;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.ReviewRepository;
import hello.yuhanTrip.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Log4j2
@RequiredArgsConstructor
public class AccommodationSettingService {

    @Value("${service.key}")
    private String SERVICE_KEY;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomReposiotry;

    private final ReviewRepository reviewRepository;



    private static final String[] IMAGE_URLS = {
            "/villa-1737168_1280.jpg",
            "/ai-generated-8856798_1280.jpg"
    };


    // 서버 시작 시 모든 숙소의 평균 평점 및 평균 가격 초기화
    @Transactional
    public void initializeAverageValues() {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        for (Accommodation accommodation : accommodations) {
            // 평균 평점이 이미 존재하는 경우 스킵
            if (accommodation.getAverageRating() == null || accommodation.getAveragePrice() == null) {
                double averageRating = calculateAverageRating(accommodation.getId());
                double averagePrice = calculateAverageRoomPrice(accommodation.getId());
                accommodation.setAverageRating(averageRating);
                accommodation.setAveragePrice(averagePrice);
                accommodationRepository.save(accommodation);
            }
        }
        log.info("모든 숙소의 평균 평점 및 평균 가격 초기화 완료");
    }




    @Transactional
    public void saveDataToDatabase() {
        List<Accommodation> allAccommodations = fetchAllDataFromAPI();
        if (!allAccommodations.isEmpty()) {
            Random random = new Random();

            // 객실 데이터 생성 및 저장
            for (Accommodation accommodation : allAccommodations) {
                // 객실 2~3개 랜덤 생성
                int roomCount = 2 + random.nextInt(2); // 2개 또는 3개

                List<Room> rooms = new ArrayList<>();
                for (int i = 0; i < roomCount; i++) {
                    Room room = new Room();
                    room.setRoomNo("R" + (1000 + i)); // 객실 번호 예: R1000, R1001, ...
                    room.setRoomNm("객실 " + (i + 1)); // 객실명 예: 객실 1, 객실 2, ...
                    room.setRoomType("Standard"); // 기본 타입, 필요에 따라 설정 가능
                    room.setMaxOccupancy(2 + random.nextInt(9)); // 최대 수용 인원: 2~10명
                    room.setRoomArea(20.0 + random.nextDouble() * 30.0); // 객실 면적: 20~50 제곱미터
                    room.setPrice(BigDecimal.valueOf(100_000 + random.nextLong(100_001))); // 가격: 100,000 ~ 200,000
                    room.setAmenities("TV, Wi-Fi"); // 기본 편의시설
                    room.setRoomIntr("편안한 객실입니다."); // 객실 소개
                    room.setRoomImgUrl(IMAGE_URLS[random.nextInt(IMAGE_URLS.length)]); // 기본 이미지 URL 랜덤 선택
                    room.setSmokingYn(random.nextBoolean()); // 흡연 가능 여부 랜덤
                    room.setBreakfastInclYn(random.nextBoolean()); // 조식 포함 여부 랜덤
                    room.setCheckInTime("10:00"); // 체크인 시간
                    room.setCheckOutTime("12:00"); // 체크아웃 시간
                    room.setAccommodation(accommodation); // 객실과 숙소 연관 설정

                    rooms.add(room);
                }

                // 저장하기
                try {
                    roomReposiotry.saveAll(rooms);
                    accommodation.setRooms(rooms); // 숙소에 객실 리스트 설정
                    accommodation.setAverageRating(0.0);
                    accommodation.setStatus(AccommodationApplyStatus.APPROVED);
                } catch (Exception e) {
                    log.error("객실 정보를 저장하는 중 오류가 발생했습니다.", e);
                    throw new RuntimeException("객실 저장 중 오류 발생", e); // 예외 던져 트랜잭션 롤백 유도
                }
            }

            try {
                accommodationRepository.saveAll(allAccommodations);
                log.info("데이터베이스에 숙소 및 객실 정보를 저장했습니다.");
            } catch (Exception e) {
                log.error("숙소 정보를 저장하는 중 오류가 발생했습니다.", e);
                throw new RuntimeException("숙소 저장 중 오류 발생", e); // 예외 던져 트랜잭션 롤백 유도
            }
        } else {
            log.info("저장할 숙소 정보가 없습니다.");
        }
    }




    public boolean isDatabaseEmpty() {
        return accommodationRepository.count() == 0;
    }

    private List<Accommodation> fetchAllDataFromAPI() {
        List<Accommodation> allAccommodations = new ArrayList<>();
        int page = 1;
        int size = 10; // 기본 페이지 사이즈

        while (true) {
            List<Accommodation> accommodations = getDataFromAPI(page, size);
            if (accommodations.isEmpty()) {
                break;
            }
            allAccommodations.addAll(accommodations);
            page++;
        }

        return allAccommodations;
    }

    private List<Accommodation> getDataFromAPI(int page, int size) {

        String encodedServiceKey = SERVICE_KEY.replace("+", "%2B"); // '+'를 '%2B'로 인코딩

        String url = String.format("https://apis.data.go.kr/B551011/KorService1/searchStay1?numOfRows=%d&pageNo=%d&MobileOS=ETC&MobileApp=Test&_type=json&serviceKey=%s", size, page, encodedServiceKey);

        // 로그 출력
        log.info("요청 URL: " + url);

        try {
            URI uri = new URI(url);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

            log.info("응답 상태 코드: " + responseEntity.getStatusCode());
            log.info("응답 본문: " + responseEntity.getBody());

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();
                // JSON 응답 파싱
                return parseResponse(responseBody);
            } else {
                log.error("API 호출이 실패했습니다. 상태 코드: " + responseEntity.getStatusCodeValue());
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.error("API 호출이 실패했습니다. 404 Not Found 에러: " + e.getMessage());
        } catch (URISyntaxException e) {
            log.error("URI 문법 오류: " + e.getMessage());
        } catch (Exception e) {
            log.error("API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }

        return Collections.emptyList();
    }
    private List<Accommodation> parseResponse(String response) {
        try {
            Map<String, Object> map = objectMapper.readValue(response, Map.class);
            Map<String, Object> responseMap = (Map<String, Object>) map.get("response");
            Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");

            Object items = bodyMap.get("items");

            // items가 빈 문자열이거나 null이면 빈 리스트 반환
            if (items == null || items instanceof String && ((String) items).isEmpty()) {
                return Collections.emptyList();
            }

            // items가 Map일 때 처리
            Map<String, Object> itemsMap = (Map<String, Object>) items;
            Object item = itemsMap.get("item");
            if (item instanceof List) {
                List<Map<String, Object>> itemList = (List<Map<String, Object>>) item;
                return itemList.stream()
                        .map(this::mapToAccommodation)
                        .filter(Objects::nonNull) // null인 경우 필터링
                        .collect(Collectors.toList());
            } else if (item instanceof Map) {
                Map<String, Object> itemMap = (Map<String, Object>) item;
                Accommodation accommodation = mapToAccommodation(itemMap);
                if (accommodation != null) {
                    return Collections.singletonList(accommodation);
                }
            }

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }

        return Collections.emptyList();
    }
    private Accommodation mapToAccommodation(Map<String, Object> item) {
        try {
            Accommodation accommodation = new Accommodation();

            accommodation.setAddr1((String) item.get("addr1"));
            accommodation.setCpyrhtDivCd((String) item.get("cpyrhtDivCd"));
            accommodation.setMapy((String) item.get("mapy"));
            accommodation.setMlevel((String) item.get("mlevel"));
            accommodation.setModifiedtime((String) item.get("modifiedtime"));
            accommodation.setSigungucode((String) item.get("sigungucode"));
            accommodation.setTel((String) item.get("tel"));
            accommodation.setTitle((String) item.get("title"));
            accommodation.setContentid((String) item.get("contentid"));
            accommodation.setContenttypeid((String) item.get("contenttypeid"));
            accommodation.setCreatedtime((String) item.get("createdtime"));
            accommodation.setBenikia((String) item.get("benikia"));
            accommodation.setGoodstay((String) item.get("goodstay"));
            accommodation.setHanok((String) item.get("hanok"));
            accommodation.setFirstimage((String) item.get("firstimage"));
            accommodation.setFirstimage2((String) item.get("firstimage2"));
            accommodation.setMapx((String) item.get("mapx"));
            accommodation.setAddr2((String) item.get("addr2"));
            accommodation.setAreacode((String) item.get("areacode"));
            accommodation.setBooktour((String) item.get("booktour"));
            accommodation.setCat1((String) item.get("cat1"));
            accommodation.setCat2((String) item.get("cat2"));
            accommodation.setCat3((String) item.get("cat3"));

            // 필수 필드 검증 추가
            if (accommodation.getAddr1() == null || accommodation.getTitle() == null) {
                return null;
            }

            return accommodation;
        } catch (Exception e) {
            log.error("Accommodation 매핑 중 오류가 발생했습니다: " + e.getMessage());
            return null;
        }
    }

    public double calculateAverageRating(Long accommodationId) {
        List<Review> reviews = reviewRepository.findByAccommodationId(accommodationId);
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public double calculateAverageRoomPrice(Long accommodationId) {
        // 해당 숙소의 Room 리스트를 조회
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 숙소가 없습니다: " + accommodationId));

        List<Room> rooms = accommodation.getRooms();

        if (rooms.isEmpty()) {
            return 0;
        }

        // Room의 price를 모두 더하고, 평균을 계산
        return rooms.stream()
                .map(Room::getPriceAsLong) // 가격을 Long으로 변환
                .filter(price -> price != null && price > 0) // null과 0 이상인 값만 필터링
                .mapToLong(Long::longValue) // LongStream으로 변환
                .average() // 평균 계산
                .orElse(0); // 결과가 없으면 0 반환
    }






}
