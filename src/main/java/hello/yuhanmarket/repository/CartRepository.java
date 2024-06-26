package hello.yuhanmarket.repository;

import hello.yuhanmarket.domain.shopping.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long> {
}
