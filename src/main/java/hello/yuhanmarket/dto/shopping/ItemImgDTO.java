package hello.yuhanmarket.dto.shopping;

import hello.yuhanmarket.domain.shopping.ItemImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class ItemImgDTO {

    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String repImgYn;

    private static ModelMapper modelMapper = new ModelMapper();

    public static ItemImgDTO of(ItemImg itemImg) {
        return modelMapper.map(itemImg, ItemImgDTO.class);
    }
}
