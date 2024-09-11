package hello.yuhanTrip.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailCertification {


    @Id
    private String certificationEmail;


    private String certificationNumber;

    private boolean checkCertification;

}
