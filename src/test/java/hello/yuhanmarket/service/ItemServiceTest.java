package hello.yuhanmarket.service;

import hello.yuhanmarket.domain.shopping.Item;
import hello.yuhanmarket.domain.shopping.ItemImg;
import hello.yuhanmarket.domain.shopping.ItemSellStatus;
import hello.yuhanmarket.dto.Item.ItemFormDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemImgRepository itemImgRepository;

    List<MultipartFile> createMultipartFiles() throws Exception {
        List<MultipartFile> multipartFileList = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            String imageName = "image" + i + ".jpg";
            MockMultipartFile multipartFile =
                    new MockMultipartFile(imageName, imageName, "image/jpg", new byte[]{1,2,3,4});
            multipartFileList.add(multipartFile);
        }
        return multipartFileList;
    }

    @Test
    @DisplayName("상품 등록 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void saveItem() throws Exception {
        // Create ItemFormDTO instance
        ItemFormDTO itemFormDto = new ItemFormDTO();
        itemFormDto.setItemNm("테스트상품");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);
        itemFormDto.setItemDetail("테스트 상품 입니다.");
        itemFormDto.setPrice(1000);
        itemFormDto.setStockNumber(100);

        // Create mock multipart files
        List<MultipartFile> multipartFileList = createMultipartFiles();

        // Call the service method
        Long itemId = itemService.saveItem(itemFormDto, multipartFileList);

        // Retrieve itemImgList ordered by imgId ascending
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByImgIdAsc(itemId);

        // Retrieve the saved Item entity
        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);

        // Assertions
        assertEquals(itemFormDto.getItemNm(), item.getItemNm());
        assertEquals(itemFormDto.getItemSellStatus(), item.getItemSellStatus());
        assertEquals(itemFormDto.getItemDetail(), item.getItemDetail());
        assertEquals(itemFormDto.getPrice(), item.getPrice());
        assertEquals(itemFormDto.getStockNumber(), item.getStockNumber());

        // Ensure itemImgList is not empty and compare file names
        assertNotNull(itemImgList);
        assertEquals(multipartFileList.size(), itemImgList.size());
        for (int i = 0; i < multipartFileList.size(); i++) {
            assertEquals(multipartFileList.get(i).getOriginalFilename(), itemImgList.get(i).getOriImgName());
        }
    }
}
