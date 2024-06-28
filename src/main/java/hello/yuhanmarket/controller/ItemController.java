package hello.yuhanmarket.controller;


import hello.yuhanmarket.dto.Item.ItemFormDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ItemController {
    @GetMapping(value = "/board/item/new")
    public String itemForm(Model model){
        model.addAttribute("itemFormDto", new ItemFormDTO());
        return "itemForm";
    }
}
