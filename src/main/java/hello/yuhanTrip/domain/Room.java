package hello.yuhanTrip.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNo;          // 객실 번호
    private String roomNm;          // 객실명
    private String roomType;        // 객실 타입
    private Integer maxOccupancy;   // 최대 수용 인원
    private Double roomArea;        // 객실 면적
    private BigDecimal price;       // 객실 1박 가격
    private String amenities;       // 편의시설
    private String roomIntr;        // 객실 소개
    private String roomImgUrl;      // 객실 이미지 URL
    private Boolean smokingYn;      // 흡연 가능 여부
    private Boolean breakfastInclYn; // 조식 포함 여부
    private String checkInTime;     // 체크인 시간
    private String checkOutTime;    // 체크아웃 시간

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation; // Accommodation과의 연관관계


    @OneToMany(mappedBy = "room")
    private List<Reservation> reservations; // Reservation과의 연관관계
    public Long getPriceAsLong() {
        if (price != null) {
            return price.longValue(); // BigDecimal을 Long으로 변환
        }
        return null; // 가격이 없을 경우 null 반환
    }


}
