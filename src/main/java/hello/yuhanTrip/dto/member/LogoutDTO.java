package hello.yuhanTrip.dto.member;


import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class LogoutDTO {

    @NotBlank
    @Email
    private String email;
}
