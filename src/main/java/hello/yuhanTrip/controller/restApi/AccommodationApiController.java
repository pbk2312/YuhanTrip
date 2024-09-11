package hello.yuhanTrip.controller.restApi;


import hello.yuhanTrip.dto.accommodation.AccommodationRegisterDTO;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodation")
public class AccommodationApiController {

    private final AccommodationServiceImpl accommodationService;

    // 숙소 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerAccommodation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @ModelAttribute AccommodationRegisterDTO dto) throws IOException {

        accommodationService.registerAccommodation(accessToken, dto);

        return ResponseEntity.ok("숙소 저장 성공");
    }
}