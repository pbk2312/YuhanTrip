package hello.yuhanTrip.repository;


import hello.yuhanTrip.domain.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetTokenReposiotry extends JpaRepository<ResetToken,Long> {

    Optional<ResetToken> findByEmail(String email);
}
