package hello.yuhanTrip.controller;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class AccommodationController {

    private final AccommodationService accommodationService;

    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping("/accommodations")
    public List<Accommodation> getAccommodations() {
        try {
            // 데이터베이스에 데이터 저장
            accommodationService.saveDataToDatabase();

            // API로부터 숙소 데이터 가져오기
            List<Accommodation> accommodations = accommodationService.getData();

            if (accommodations.isEmpty()) {
                System.out.println("조회된 숙소 정보가 없습니다.");
            }

            return accommodations;
        } catch (Exception e) {
            // 예외 처리: 로깅 및 예외 상황에 대한 응답 처리
            System.err.println("숙소 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace(); // 로깅: 발생한 예외를 콘솔에 출력
            return Collections.emptyList(); // 오류 발생 시 빈 리스트 반환
        }
    }
}
