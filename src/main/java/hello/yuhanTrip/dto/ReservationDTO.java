package hello.yuhanTrip.dto;


import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class ReservationDTO {

    @NotBlank
    private String reservationName;










}
