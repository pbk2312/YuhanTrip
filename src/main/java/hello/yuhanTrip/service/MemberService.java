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
    /**
     * 회원 가입
     *
     * @param memberRequestDTO 회원 가입 요청 DTO
     * @return 성공 메시지
     */
    String register(MemberRequestDTO memberRequestDTO);

    /**
     * 로그인
     *
     * @param loginDTO 로그인 요청 DTO
     * @return 로그인 성공 시 발급된 토큰 DTO
     */
    TokenDTO login(LoginDTO loginDTO);

    /**
     * 로그아웃
     *
     * @param logoutDTO 로그아웃 요청 DTO
     */
    void logout(LogoutDTO logoutDTO);

    /**
     * 비밀번호 재설정 이메일 전송
     *
     * @param emailRequestDTO 비밀번호 재설정 이메일 요청 DTO
     * @return 이메일 전송 성공 메시지
     */
    String sendPasswordResetEmail(EmailRequestDTO emailRequestDTO);

    /**
     * 비밀번호 변경
     *
     * @param memberChangePasswordDTO 비밀번호 변경 DTO
     */
    void memberChangePassword(MemberChangePasswordDTO memberChangePasswordDTO);

    /**
     * 회원 탈퇴
     *
     * @param withdrawalMembershipDTO 회원 탈퇴 DTO
     * @return 탈퇴 성공 메시지
     */
    String deleteAccount(WithdrawalMembershipDTO withdrawalMembershipDTO);

    /**
     * 회원 정보 업데이트
     *
     * @param member 업데이트할 회원 정보
     */
    void updateMember(Member member);

    /**
     * 호스트 권한 확인
     *
     * @param memberId 회원 ID
     * @return 호스트 권한을 가진 회원
     */
    Member validateHost(Long memberId);

    /**
     * 회원 ID로 소속된 숙소 목록 조회
     *
     * @param memberId 회원 ID
     * @return 해당 회원이 소속된 숙소 목록
     */
    List<Accommodation> getAccommodationsByMemberId(Long memberId);

    /**
     * 이메일로 회원 조회
     *
     * @param email 회원 이메일
     * @return 해당 이메일의 회원
     */
    Member findByEmail(String email);
}
