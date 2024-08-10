package hello.yuhanTrip.dto;

import hello.yuhanTrip.domain.CancelReservation;
import hello.yuhanTrip.domain.MemberLike;
import hello.yuhanTrip.domain.MemberRole;
import hello.yuhanTrip.domain.Reservation;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

public class MypageMemberDTO {

    private String email;
    private String password;

    private MemberRole memberRole;



    private String name;
    private String nickname;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;


}
