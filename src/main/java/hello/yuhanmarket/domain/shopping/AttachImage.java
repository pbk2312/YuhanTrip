package hello.yuhanmarket.domain.shopping;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "ATTACHMENT_SEQ_GENERATOR",
        sequenceName = "ATTACHMENT_SEQ"
)
public class AttachImage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String originFilename;
    private String storeFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder
    public AttachImage(Long id, String originFilename, String storeFilename, Board board) {
        this.id = id;
        this.originFilename = originFilename;
        this.storeFilename = storeFilename;
        this.board = board;
    }
}
