package hello.yuhanmarket.repository;


import hello.yuhanmarket.domain.shopping.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Long> {

}
