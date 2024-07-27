package hello.yuhanTrip.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Accommodation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private int price; // 1박 가격

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;

}

