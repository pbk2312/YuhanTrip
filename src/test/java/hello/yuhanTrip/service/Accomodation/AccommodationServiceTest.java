package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Accommodation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccommodationServiceTest {

    @Autowired
    AccommodationServiceImpl accommodationService;

    @Test
    public void getAccommodationInfo() {

        Long id = 1L;
        Accommodation accommodationInfo = accommodationService.getAccommodationInfo(id);
        System.out.println(accommodationInfo.getTitle());

    }
}