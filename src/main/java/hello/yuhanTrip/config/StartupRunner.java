package hello.yuhanTrip.config;

import hello.yuhanTrip.service.Accomodation.AccommodationService;
import hello.yuhanTrip.service.Accomodation.AccommodationSettingService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupRunner {

    private final AccommodationSettingService accommodationService;

    public StartupRunner(AccommodationSettingService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            if (accommodationService.isDatabaseEmpty()) { // 데이터베이스가 비어있는지 체크
                accommodationService.saveDataToDatabase();
                System.out.println("데이터가 DB에 저장되었습니다.");
            } else {
                System.out.println("데이터베이스에 이미 저장된 숙소 정보가 있습니다. 초기화 작업을 스킵합니다.");
            }

            // 모든 숙소의 평균 평점 및 평균 가격 초기화
            accommodationService.initializeAverageValues();
            System.out.println("모든 숙소의 평균 평점 및 평균 가격 초기화 완료");
        };
    }
}
