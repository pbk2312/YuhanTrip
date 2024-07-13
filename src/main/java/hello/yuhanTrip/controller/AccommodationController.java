package hello.yuhanTrip.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
//
@RestController
public class AccommodationController {


    private final AccommodationService accommodationService;

    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping("/accommodations")
    public List<Map<String, Object>> getAccommodations(
            @RequestParam int areaCode,
            @RequestParam String state,
            @RequestParam int contentTypeId,
            @RequestParam int numOfRows) throws URISyntaxException, JsonProcessingException {
        return accommodationService.getData(areaCode, state, contentTypeId, numOfRows);
    }




}
