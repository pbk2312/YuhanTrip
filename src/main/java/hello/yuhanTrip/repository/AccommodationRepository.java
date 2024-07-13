package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation,Long> {

}
