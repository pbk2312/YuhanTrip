package hello.yuhanTrip.repository;


import hello.yuhanTrip.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 중복 가입 방지
    Optional<Member> findByEmail(String email);

    // 존재 여부
    boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.password = :newPassword WHERE m.email = :email")
    void updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);


    // 회원 정보 업데이트
    // 회원 정보 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.name = :name, m.nickname = :nickname, m.phoneNumber = :phoneNumber, " +
            "m.dateOfBirth = :dateOfBirth, m.address = :address WHERE m.email = :email")
    void updateMemberInfo(@Param("email") String email,
                          @Param("name") String name,
                          @Param("nickname") String nickname,
                          @Param("phoneNumber") String phoneNumber,
                          @Param("dateOfBirth") LocalDate dateOfBirth,
                          @Param("address") String address);
}
