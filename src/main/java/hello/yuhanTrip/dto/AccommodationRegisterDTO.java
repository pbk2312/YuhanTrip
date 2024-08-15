package hello.yuhanTrip.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class AccommodationRegisterDTO {
    private String addr1;          // 주소
    private String addr2;          // 상세 주소
    private String mapx;           // 지도 X 좌표
    private String mapy;           // 지도 Y 좌표
    private String tel;            // 전화번호
    private String title;          // 숙소 이름
    private String sigungucode;    // 시군구 코드

    private List<MultipartFile> images; // 여러 개의 이미지 파일을 받을 수 있도록 List로 설정

    private List<RoomDTO> rooms; // 객실 정보 리스트
}
