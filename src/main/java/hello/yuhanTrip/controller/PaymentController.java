package hello.yuhanTrip.controller;


import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Reservation;
import hello.yuhanTrip.repository.AccommodationRepository;
import hello.yuhanTrip.repository.ReservationRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PaymentController {


    private final ReservationRepository reservationRepository;

    @GetMapping("/checkout")
    public String paymentCheckout(
            @RequestParam("/reservationId") Long reservationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        Reservation reservaionInfo = reservationRepository.getReferenceById(reservationId);


        if (userDetails == null) {
            return "redirect:/member/login";
        }


        log.info("예약자 이메일 : {}", userDetails.getUsername());


        log.info("숙소 예약자 : {} ", reservaionInfo.getName());


        return "paymentPage";

    }


}
