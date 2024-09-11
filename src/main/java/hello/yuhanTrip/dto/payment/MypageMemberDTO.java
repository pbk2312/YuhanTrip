package hello.yuhanTrip.dto.payment;


import lombok.*;


import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MypageMemberDTO {

    private String email;

    private String name;
    private String nickname;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;


}
