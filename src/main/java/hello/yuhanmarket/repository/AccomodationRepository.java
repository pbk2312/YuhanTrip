package hello.yuhanmarket.repository;

import hello.yuhanmarket.domain.Accomodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccomodationRepository extends JpaRepository<Accomodation,Long> {

}
