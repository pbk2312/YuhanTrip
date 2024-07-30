package hello.yuhanTrip.service.Accomodation;

import hello.yuhanTrip.domain.Reservation;
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

    public void reservationRegister(Reservation reservation) {
        log.info("숙소 예약 저장");
        Reservation reservationSet = reservationRepository.save(reservation);

        log.info("숙소 예약 성공 : {}" ,reservationSet.getAccommodation());


    }

    public boolean isDateOverlapping(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(accommodationId, checkInDate, checkOutDate);
        boolean isOverlapping = !overlappingReservations.isEmpty();

        if (isOverlapping) {
            log.info("숙소 ID {}에 대해 겹치는 예약이 발견되었습니다: {}", accommodationId, overlappingReservations);
        } else {
            log.info("숙소 ID {}에 대해 겹치는 예약이 없습니다.", accommodationId);
        }

        return isOverlapping;
    }
}
