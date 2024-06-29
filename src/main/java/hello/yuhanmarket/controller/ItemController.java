package hello.yuhanmarket.controller;

import hello.yuhanmarket.domain.Member;
import hello.yuhanmarket.domain.MemberRole;
import hello.yuhanmarket.dto.Item.ItemFormDTO;
import hello.yuhanmarket.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final MemberRepository memberRepository;

    @GetMapping(value = "/admin/item/new")
    public String itemForm(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Could not find user for " + userDetails.getUsername()));

        if (member.getMemberRole() != MemberRole.ADMIN) {
            throw new RuntimeException("Access denied: You do not have permission to access this resource");
        }

        model.addAttribute("itemFormDto", new ItemFormDTO());
        return "itemForm";
    }
}
