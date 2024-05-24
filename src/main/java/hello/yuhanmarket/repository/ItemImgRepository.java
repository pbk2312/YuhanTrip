package hello.yuhanmarket.repository;

import hello.yuhanmarket.domain.shopping.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemImgRepository extends JpaRepository<ItemImage,Long> {

}
