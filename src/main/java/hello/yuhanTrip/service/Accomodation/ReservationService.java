package hello.yuhanTrip.service.Accomodation;


import hello.yuhanTrip.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReservationService {

    private final ReservationRepository reservationRepository;


    public List<LocalDate> getBookedDates(Long accommodationId) {
        log.info("Fetching booked dates for accommodation id: {}", accommodationId);
        return reservationRepository.findBookedDatesByAccommodationId(accommodationId);
    }


}
