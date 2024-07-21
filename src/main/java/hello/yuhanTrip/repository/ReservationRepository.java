package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ReservationRepository extends JpaRepository<Reservation,Long> {
}
