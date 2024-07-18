package hello.yuhanTrip.config;

import hello.yuhanTrip.service.Accomodation.AccommodationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupRunner {

    private final AccommodationService accommodationService;

    public StartupRunner(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            if (isDatabaseEmpty()) { // 데이터베이스가 비어있는지 체크
                accommodationService.saveDataToDatabase();
                System.out.println("데이터가 DB에 저장되었습니다.");
            } else {
                System.out.println("데이터베이스에 이미 저장된 숙소 정보가 있습니다. 초기화 작업을 스킵합니다.");
            }
        };
    }

    private boolean isDatabaseEmpty() {
        // 여기에서 데이터베이스에 저장된 숙소 정보가 있는지 체크하는 로직을 구현합니다.
        // 예를 들어, accommodationService.getAllData() 메소드를 사용하여 데이터 유무를 확인할 수 있습니다.
        // 데이터가 존재하면 true, 없으면 false를 반환하도록 구현합니다.
        return accommodationService.getAllData().isEmpty();
    }
}
