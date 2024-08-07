package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.PaymentAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAccessTokenRepository extends JpaRepository<PaymentAccessToken,Long> {


    Optional<PaymentAccessToken> findByReservationId(Long reservationId);
}
