package hello.yuhanmarket.controller;


import hello.yuhanmarket.dto.Item.ItemFormDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/board")
public class ItemController {

    @GetMapping("/item/new")
    public String itemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDTO());
        return "itemForm";
    }


}
