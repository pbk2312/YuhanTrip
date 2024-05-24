package hello.yuhanmarket.domain.shopping;


import hello.yuhanmarket.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String writer;
    private String title;
    private String content;
    private LocalDateTime writeTime;
    private Integer price;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL) // 일대다
    private List<ItemImage> attachedFiles = new ArrayList<>();



}
