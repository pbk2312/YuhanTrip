package hello.yuhanmarket.repository;


import hello.yuhanmarket.domain.shopping.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderReposiotry extends JpaRepository<Order,Long> {
}
