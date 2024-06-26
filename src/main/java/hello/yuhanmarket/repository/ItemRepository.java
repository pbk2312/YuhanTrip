package hello.yuhanmarket.repository;

import hello.yuhanmarket.domain.shopping.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long> {
    List<Item> findByItemNm(String itemNm);
}
