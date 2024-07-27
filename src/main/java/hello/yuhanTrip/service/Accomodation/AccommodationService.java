package hello.yuhanTrip.service.Accomodation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.repository.AccommodationRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AccommodationService {

    @Value("${service.key}")
    private String SERVICE_KEY;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AccommodationRepository accommodationRepository;

    public AccommodationService(RestTemplate restTemplate, ObjectMapper objectMapper, AccommodationRepository accommodationRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.accommodationRepository = accommodationRepository;
    }


    @Transactional
    public void saveDataToDatabase() {
        List<Accommodation> allAccommodations = fetchAllDataFromAPI();
        if (!allAccommodations.isEmpty()) {
            // Random 객체 생성
            Random random = new Random();

            // 각 숙박 정보에 대해 랜덤 가격 설정
            for (Accommodation accommodation : allAccommodations) {
                int randomPrice = 100_000 + random.nextInt(100_001); // 100000 ~ 200000 사이의 랜덤 값 생성
                accommodation.setPrice(randomPrice);
            }

            try {
                accommodationRepository.saveAll(allAccommodations);
                log.info("데이터베이스에 숙소 정보를 저장했습니다.");
            } catch (Exception e) {
                log.info("데이터베이스에 숙소 정보를 저장하는 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            log.info("저장할 숙소 정보가 없습니다.");
        }
    }

    public Page<Accommodation> getAccommodations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accommodationRepository.findAll(pageable);
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
            Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");

            if (itemsMap == null) {
                return Collections.emptyList();
            }

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


    public Accommodation getAccommodationInfo(Long id) {

        Accommodation accommodation = accommodationRepository.findById(id).orElseThrow(() -> new RuntimeException("존재하지 않는 숙소입니다."));

        log.info("숙소 이름 = {}", accommodation.getTitle());

        return accommodation;


    }

}
