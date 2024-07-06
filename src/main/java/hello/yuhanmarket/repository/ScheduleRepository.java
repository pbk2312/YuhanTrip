package hello.yuhanmarket.repository;

import hello.yuhanmarket.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> {


}
