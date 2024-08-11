package hello.yuhanTrip.dto;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewWriteDTO {



    private String content;  // 리뷰 내용
    private int rating;      // 별점 (1-5)
    private LocalDate reviewDate;  // 리뷰 작성일




}
