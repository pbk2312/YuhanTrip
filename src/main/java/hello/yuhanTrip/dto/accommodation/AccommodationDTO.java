package hello.yuhanTrip.dto.accommodation;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationDTO {
    private Long id;
    private String addr1;
    private String addr2;
    private String areacode;
    private String benikia;
    private String cat1;
    private String cat2;
    private String cat3;
    private String contentid;
    private String contenttypeid;
    private String createdtime;
    private String firstimage;
    private String firstimage2;
    private String cpyrhtDivCd;
    private String goodstay;
    private String hanok;
    private String mapx;
    private String mapy;
    private String mlevel;
    private String modifiedtime;
    private String tel;
    private String title;
    private String booktour;
    private String sigungucode;
    private int reviewCount;
    private Double averageRating;
    private Double averagePrice;
    private String status;
    private String type;
    private Long memberId;

}