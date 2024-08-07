package hello.yuhanTrip.repository;


import hello.yuhanTrip.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long>{


    // paymentUid를 기반으로 Payment 엔티티를 조회하는 메서드
    Payment findByPaymentUid(String paymentUid);
}
