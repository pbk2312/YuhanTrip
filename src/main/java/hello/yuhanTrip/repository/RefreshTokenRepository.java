package hello.yuhanTrip.repository;


import hello.yuhanTrip.domain.member.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken,String> {


    void deleteByEmail(String email); // 새로운 삭제 메서드 추가
}
