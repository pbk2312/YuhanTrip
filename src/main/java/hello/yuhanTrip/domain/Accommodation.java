package hello.yuhanTrip.domain;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

    private int reviewCount;
    private Double averageRating;
    private Double averagePrice;

    @Enumerated(EnumType.STRING)
    private AccommodationApplyStatus status;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Room> rooms = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference
    private Member member;  // 숙소를 등록한 멤버(호스트)

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLike> memberLikes; // 추가된 부분

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;  // 추가된 부분





}

