package hello.yuhanTrip.service;

import hello.yuhanTrip.domain.Accommodation;
import hello.yuhanTrip.domain.Member;
import hello.yuhanTrip.dto.LoginDTO;
import hello.yuhanTrip.dto.LogoutDTO;
import hello.yuhanTrip.dto.WithdrawalMembershipDTO;
import hello.yuhanTrip.dto.email.EmailRequestDTO;
import hello.yuhanTrip.dto.register.MemberChangePasswordDTO;
import hello.yuhanTrip.dto.register.MemberRequestDTO;
import hello.yuhanTrip.dto.token.TokenDTO;

import java.util.List;

public interface MemberService {

    // 회원 가입을 처리하는 메서드
    String register(MemberRequestDTO memberRequestDTO);

    // 로그인 요청을 처리하고, 인증 토큰을 반환하는 메서드
    TokenDTO login(LoginDTO loginDTO);

    // 로그아웃 요청을 처리하는 메서드
    void logout(LogoutDTO logoutDTO);

    // 비밀번호 재설정을 위한 이메일을 발송하는 메서드
    String sendPasswordResetEmail(EmailRequestDTO emailRequestDTO);

    // 회원의 비밀번호를 변경하는 메서드
    void memberChangePassword(MemberChangePasswordDTO memberChangePasswordDTO);

    // 회원 탈퇴를 처리하는 메서드
    String deleteAccount(WithdrawalMembershipDTO withdrawalMembershipDTO);

    // 회원 정보를 업데이트하는 메서드
    void updateMember(Member member);

    // 호스트로서 유효한 회원인지 확인하는 메서드
    Member validateHost(Long memberId);

    // 특정 회원 ID에 의해 등록된 숙소 목록을 반환하는 메서드
    List<Accommodation> getAccommodationsByMemberId(Long memberId);

    // 이메일로 회원 정보를 조회하는 메서드
    Member findByEmail(String email);
}