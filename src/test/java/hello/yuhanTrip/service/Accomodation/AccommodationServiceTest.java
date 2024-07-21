package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.repository.AccommodationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccommodationServiceTest {

    @Autowired
    AccommodationService accommodationService;

    @Test
    public void getAccommodationInfo() {

        Long id = 1L;
        Accommodation accommodationInfo = accommodationService.getAccommodationInfo(id);
        System.out.println(accommodationInfo.getTitle());

    }
}