package hello.yuhanmarket.repository;


import hello.yuhanmarket.domain.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetTokenReposiotry extends JpaRepository<ResetToken,Long> {

}
