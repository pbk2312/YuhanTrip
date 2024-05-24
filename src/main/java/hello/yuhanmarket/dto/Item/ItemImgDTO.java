package hello.yuhanmarket.dto.Item;


import hello.yuhanmarket.domain.shopping.ItemImage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
@NoArgsConstructor
public class ItemImgDTO {


    private Long id;
    private String imgName;
    private String oriImgName;
    private String imgUrl;
    private String repImgYn;

    // 생성자를 통해 ModelMapper 객체를 받아올 수 있도록 변경
    public ItemImgDTO(ItemImage itemImg, ModelMapper modelMapper) {
        modelMapper.map(itemImg, this);
    }


    }

