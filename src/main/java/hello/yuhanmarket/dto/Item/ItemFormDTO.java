package hello.yuhanmarket.dto.Item;


import hello.yuhanmarket.domain.shopping.Item;
import hello.yuhanmarket.domain.shopping.ItemImage;
import hello.yuhanmarket.domain.shopping.ItemSellStatus;
import hello.yuhanmarket.dto.Item.ItemImgDTO;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemFormDTO {
    private Long id;

    @NotBlank(message = "상품명은 필수 입력값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력값입니다.")
    private Integer price;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    // 상품 저장 후 수정할 때 상품 이미지 정보를 저장하는 리스트
    private List<ItemImgDTO> itemImgDtoList = new ArrayList<>();

    // 상품의 이미지 아이디를 저장하는 리스트
    // 상품 등록 시 상품의 이미지를 저장X
    // 수정 시 이미지 아이디를 담아둥 용도
    private List<Long> itemImgIds = new ArrayList<>();

    // ModelMapper 인스턴스 생성
    private static final ModelMapper modelMapper = new ModelMapper();

    // ItemFormDTO 객체를 Item 엔터티로 변환하는 메서드
    public Item toItemEntity() {
        Item item = modelMapper.map(this, Item.class);

        // ItemFormDTO의 itemImgDtoList를 Item 엔터티의 attachedFiles로 매핑
        List<ItemImage> attachedFiles = new ArrayList<>();
        for (ItemImgDTO itemImgDTO : this.itemImgDtoList) {
            attachedFiles.add(modelMapper.map(itemImgDTO, ItemImage.class));
        }
        item.setAttachedFiles(attachedFiles);

        return item;
    }

    // Item 엔터티를 ItemFormDTO 객체로 변환하는 메서드
    public static ItemFormDTO fromItemEntity(Item item) {
        ItemFormDTO itemFormDTO = modelMapper.map(item, ItemFormDTO.class);

        // Item 엔터티의 attachedFiles를 ItemFormDTO의 itemImgDtoList로 매핑
        List<ItemImgDTO> itemImgDTOList = new ArrayList<>();
        for (ItemImage itemImage : item.getAttachedFiles()) {
            itemImgDTOList.add(modelMapper.map(itemImage, ItemImgDTO.class));
        }
        itemFormDTO.setItemImgDtoList(itemImgDTOList);

        return itemFormDTO;
    }
}
