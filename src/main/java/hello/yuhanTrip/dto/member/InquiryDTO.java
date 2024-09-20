package hello.yuhanTrip.dto.member;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InquiryDTO {


    private String name;
    private String subject;
    private String message;
    private LocalDate crateAt;
}
