package hello.yuhanmarket.service;


import hello.yuhanmarket.domain.Schedule;
import hello.yuhanmarket.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {


    private final ScheduleRepository scheduleRepository;


    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }


}
