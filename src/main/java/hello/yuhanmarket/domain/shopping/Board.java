package hello.yuhanmarket.domain.shopping;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private int price;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<AttachImage> attachedFiles = new ArrayList<>();



}
