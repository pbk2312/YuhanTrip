package hello.yuhanTrip.service.Accomodation;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.domain.Accommodation;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccommodationService {

    private final String SERVICE_KEY = "1jOaYO0XR8PMGPAbu9weSps9WLKcVHuuGNYDQYPQpb8MtqQAsQhVr8vFxqxXdnIK2jxJet+xzUADfGo1eJLhkA==";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AccommodationRepository accommodationRepository;

    public AccommodationService(RestTemplate restTemplate, ObjectMapper objectMapper, AccommodationRepository accommodationRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.accommodationRepository = accommodationRepository;
    }

    public List<Map<String, Object>> getData(int areaCode, String state, int contentTypeId, int numOfRows) throws URISyntaxException, JsonProcessingException {
        String url = "https://apis.data.go.kr/B551011/KorService1/searchStay1";
        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Test")
                .queryParam("_type", "json")
                .queryParam("numOfRows", numOfRows)
                .queryParam("serviceKey", SERVICE_KEY)
                .build()
                .toUri();

        String response = restTemplate.getForObject(uri, String.class);
        Map<String, Object> map = objectMapper.readValue(response, Map.class);
        Map<String, Object> responseMap = (Map<String, Object>) map.get("response");
        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("body");
        Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
        List<Map<String, Object>> itemMap = (List<Map<String, Object>>) itemsMap.get("item");

        // state에 있는 정보만 필터링
        List<Map<String, Object>> filteredItems = itemMap.stream()
                .filter(item -> {
                    Object value = item.get("addr1");
                    return value != null && value.toString().contains(state);
                })
                .collect(Collectors.toList());

        // 데이터베이스에 저장
        List<Accommodation> accommodations = filteredItems.stream().map(this::mapToAccommodation).collect(Collectors.toList());
        accommodationRepository.saveAll(accommodations);

        return filteredItems;
    }

    private Accommodation mapToAccommodation(Map<String, Object> item) {
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
        return accommodation;
    }



}
