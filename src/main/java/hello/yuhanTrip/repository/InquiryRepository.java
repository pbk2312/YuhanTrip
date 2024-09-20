package hello.yuhanTrip.repository;

import hello.yuhanTrip.domain.member.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry,Long> {

}
