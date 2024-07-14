package hello.yuhanTrip.service.Accomodation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.repository.AccommodationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
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
    public void saveDataToDatabase(int numOfRows) {
        List<Accommodation> accommodations = getData(numOfRows);
        if (!accommodations.isEmpty()) {
            accommodationRepository.saveAll(accommodations);
            System.out.println("데이터베이스에 숙소 정보를 저장했습니다.");
        } else {
            System.out.println("저장할 숙소 정보가 없습니다.");
        }
    }

    public List<Accommodation> getData(int numOfRows) {
        String url = "https://apis.data.go.kr/B551011/KorService1/searchStay1";

        // Build URI with parameters
        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Test")
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("_type", "json")
                .queryParam("numOfRows", numOfRows)
                .build()
                .toUri();

        // 로그 출력
        System.out.println("Request URI: " + uri.toString());

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

            System.out.println("Response Status Code: " + responseEntity.getStatusCode());
            System.out.println("Response Body: " + responseEntity.getBody());

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();
                // Parse JSON response
                return parseResponse(responseBody);
            } else {
                System.err.println("API 호출이 실패했습니다. 상태 코드: " + responseEntity.getStatusCodeValue());
            }
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("API 호출이 실패했습니다. 404 Not Found 에러: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("API 호출 중 오류가 발생했습니다: " + e.getMessage());
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
            System.err.println("JSON 파싱 중 오류가 발생했습니다: " + e.getMessage());
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
            System.err.println("Accommodation 매핑 중 오류가 발생했습니다: " + e.getMessage());
            return null;
        }
    }
}
