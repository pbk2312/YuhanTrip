//package hello.yuhanmarket.repository;
//
//import hello.yuhanmarket.domain.shopping.Item;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class BoardRepositoryTest {
//
//    @Autowired
//    private BoardRepository boardRepository;
//
//    @Test
//    @Transactional
//    void save() {
//        // given
//        Item board = Item.builder()
//                .title("테스트 상품")
//                .content("테스트 상품입니다")
//                .price(10000)
//                .build();
//
//        // when
//        Item savedBoard = boardRepository.save(board);
//
//        // then
//        assertNotNull(savedBoard.getId());
//        assertEquals(board.getTitle(), savedBoard.getTitle());
//        assertEquals(board.getContent(), savedBoard.getContent());
//        assertEquals(board.getPrice(), savedBoard.getPrice());
//
//        System.out.println(savedBoard);
//    }
//}