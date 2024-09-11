package hello.yuhanTrip.controller.view;

import hello.yuhanTrip.domain.accommodation.Accommodation;
import hello.yuhanTrip.domain.member.Member;
import hello.yuhanTrip.domain.reservation.Reservation;
import hello.yuhanTrip.dto.accommodation.ReservationDTO;
import hello.yuhanTrip.dto.payment.PaymentCancelDTO;
import hello.yuhanTrip.dto.payment.PaymentDTO;
import hello.yuhanTrip.repository.ReservationRepository;
import hello.yuhanTrip.service.Accomodation.AccommodationServiceImpl;
import hello.yuhanTrip.service.Accomodation.ReservationService;
import hello.yuhanTrip.service.member.MemberService;
import hello.yuhanTrip.service.reservation.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@Log4j2
public class PaymentViewController {

    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final AccommodationServiceImpl accommodationService;
    private final MemberService memberService;



    // 결제 페이지
    @GetMapping("/paymentPage")
    public String paymentPage(
            @RequestParam("reservationId") Long reservationId,
            @RequestParam("totalPrice") Long totalPrice,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {

        Member member = memberService.getUserDetails(accessToken);
        log.info("예약 시도 유저 : {}", member.getEmail());

        // 예약 정보 조회
        Reservation reservationInfo = reservationRepository.findById(reservationId).orElse(null);

        if (reservationInfo == null) {
            log.error("예약 정보를 찾을 수 없습니다. reservationId: {}", reservationId);
            return "redirect:/error"; // 예약 정보가 없을 경우 오류 페이지로 리다이렉트
        }
        String reservationUid = reservationInfo.getReservationUid();
        log.info("reservationUid: {}", reservationUid);

        PaymentDTO requestDto = paymentService.findRequestDto(reservationUid);
        requestDto.setAddr(member.getEmail());


        log.info("paymentRequest = {} ", requestDto);

        // 예약 정보를 PaymentDTO에 담기
        // 모델에 PaymentDTO 추가
        model.addAttribute("requestDto", requestDto);


        // 결제 페이지로 이동
        return "paymentPage";
    }






    // 결제 취소 페이지
    @GetMapping("/paymentCancelPage")
    public String paymentCancel(@RequestParam("reservationId") Long reservationId,
                                @CookieValue(value = "accessToken", required = false) String accessToken,
                                Model model) {

        log.info("환불 받을 예약 번호: {}", reservationId);

        Member member = memberService.getUserDetails(accessToken);

        log.info("환불 신청 유저 : {}" ,member.getEmail());
        // 1. 예약 정보 조회
        Reservation reservation = reservationService.findReservation(reservationId);
        Accommodation accommodation = accommodationService.getAccommodationInfo(reservation.getAccommodationId());

        // 2. 결제 정보 조회
        String paymentUid = reservation.getPayment().getPaymentUid();
        hello.yuhanTrip.domain.reservation.Payment payment = paymentService.findPayment(paymentUid);

        // 3. PaymentCancelDTO 생성
        PaymentCancelDTO paymentCancelDTO = new PaymentCancelDTO();
        paymentCancelDTO.setPaymentUid(paymentUid);
        paymentCancelDTO.setCancelRequestAmount(payment.getPrice());
        paymentCancelDTO.setReservationId(reservationId);

        // 4. ReservationDTO 생성
        ReservationDTO reservationDTO = ReservationDTO.builder()
                .id(reservation.getId())
                .memberId(reservation.getMember().getId())
                .accommodationId(reservation.getAccommodationId())
                .roomId(reservation.getRoom().getId())
                .roomNm(reservation.getRoom().getRoomNm())
                .roomType(reservation.getRoom().getRoomType())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .reservationDate(reservation.getReservationDate())
                .specialRequests(reservation.getSpecialRequests())
                .accommodationTitle(accommodation.getTitle())
                .localDate(reservation.getReservationDate())
                .name(reservation.getMember().getName())
                .phoneNumber(reservation.getMember().getPhoneNumber())
                .price(reservation.getPayment().getPrice())
                .addr(reservation.getMember().getAddress())
                .numberOfGuests(reservation.getNumberOfGuests())
                .build();

        // 5. 모델에 데이터 추가
        model.addAttribute("reservationDTO", reservationDTO);
        model.addAttribute("paymentCancelDTO", paymentCancelDTO);

        // 6. 뷰로 반환
        return "reservation/reservationCancel";
    }






}
