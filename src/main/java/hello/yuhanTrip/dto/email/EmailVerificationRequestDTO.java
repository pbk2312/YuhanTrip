package hello.yuhanTrip.dto.email;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class EmailVerificationRequestDTO {

    @NotBlank
    @Email
    private String email;

    private String certificationNumber;
}
