package hello.yuhanTrip.repository;


import hello.yuhanTrip.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
