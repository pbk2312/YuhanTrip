package hello.yuhanTrip.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomDTO {
    private String roomNo;          // 객실 번호
    private String roomNm;          // 객실명
    private String roomType;        // 객실 타입
    private Integer maxOccupancy;   // 최대 수용 인원
    private Double roomArea;        // 객실 면적
    private BigDecimal price;       // 객실 1박 가격
    private String amenities;       // 편의시설
    private String roomIntr;        // 객실 소개
    private MultipartFile roomImg;  // 객실 이미지 파일
    private Boolean smokingYn;      // 흡연 가능 여부
    private Boolean breakfastInclYn; // 조식 포함 여부
    private String checkInTime;     // 체크인 시간
    private String checkOutTime;    // 체크아웃 시간
}
